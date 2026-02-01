package com.example.demo.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 最小化 JVM 启动器（简化版）
 *
 * 职责：
 * - 基于 {@link DependencyAnalyzer} 输出的类资源集合，预加载必要的 .class 字节到内存
 * - 使用自定义 ClassLoader 在“瘦身”环境中执行指定方法
 * - 为 DebugSession 提供一次性执行能力（当前仅记录根方法这一个 step，便于前端回放）
 */
public class MinimalJVMStarter {

    private final Set<String> requiredResources;
    private final Map<String, byte[]> classCache = new HashMap<>();

    public MinimalJVMStarter(Set<String> requiredResources) {
        this.requiredResources = requiredResources;
    }

    /**
     * 在最小 JVM 环境中执行指定方法（一次性执行）
     *
     * @param className  目标类
     * @param methodName 方法名
     * @param args       参数
     */
    public Object invokeOnce(String className, String methodName, Object... args) throws Exception {
        loadRequiredClasses();

        CustomClassLoader classLoader = new CustomClassLoader();
        Class<?> clazz = classLoader.loadClass(className);
        Method method = findMethod(clazz, methodName, args);
        if (method == null) {
            throw new NoSuchMethodException("Method " + methodName + " not found in class " + className);
        }

        boolean isStatic = java.lang.reflect.Modifier.isStatic(method.getModifiers());
        Object target = isStatic ? null : clazz.getDeclaredConstructor().newInstance();
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    /**
     * 使用 DependencyAnalyzer 解析出的 MethodMeta 调用方法
     * 更适合处理带有精确参数类型（含基本类型）的场景
     */
    public Object invokeOnce(DependencyAnalyzer.MethodMeta meta, Object... args) throws Exception {
        loadRequiredClasses();

        CustomClassLoader classLoader = new CustomClassLoader();
        Class<?> clazz = classLoader.loadClass(meta.getClassName());

        Method method;
        if (!meta.getParameterTypeNames().isEmpty()) {
            // 按签名精确匹配（支持基本类型）
            Class<?>[] paramTypes = new Class<?>[meta.getParameterTypeNames().size()];
            for (int i = 0; i < meta.getParameterTypeNames().size(); i++) {
                paramTypes[i] = Class.forName(meta.getParameterTypeNames().get(i));
            }
            method = clazz.getDeclaredMethod(meta.getMethodName(), paramTypes);
        } else {
            // 回退到按实参类型匹配
            method = findMethod(clazz, meta.getMethodName(), args);
        }

        if (method == null) {
            throw new NoSuchMethodException(
                    "Method " + meta.getMethodName() + " not found in class " + meta.getClassName());
        }

        boolean isStatic = java.lang.reflect.Modifier.isStatic(method.getModifiers());
        Object target = isStatic ? null : clazz.getDeclaredConstructor().newInstance();
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    /**
     * 加载必须的类字节码到内存缓存
     */
    private void loadRequiredClasses() throws IOException {
        String classpath = System.getProperty("java.class.path");
        String[] paths = classpath.split(File.pathSeparator);

        for (String path : paths) {
            File file = new File(path);
            if (file.isFile() && file.getName().endsWith(".jar")) {
                try (JarFile jarFile = new JarFile(file)) {
                    for (String resource : requiredResources) {
                        if (!classCache.containsKey(resource)) {
                            byte[] bytes = extractClassFromJar(jarFile, resource);
                            if (bytes != null) {
                                classCache.put(resource, bytes);
                            }
                        }
                    }
                }
            } else if (file.isDirectory()) {
                for (String resource : requiredResources) {
                    if (classCache.containsKey(resource)) {
                        continue;
                    }
                    File classFile = new File(file, resource);
                    if (classFile.exists()) {
                        try (java.io.InputStream is = new java.io.FileInputStream(classFile)) {
                            classCache.put(resource, is.readAllBytes());
                        }
                    }
                }
            }
        }
    }

    private byte[] extractClassFromJar(JarFile jarFile, String resourcePath) throws IOException {
        JarEntry entry = jarFile.getJarEntry(resourcePath);
        if (entry == null) {
            return null;
        }
        try (java.io.InputStream is = jarFile.getInputStream(entry)) {
            return is.readAllBytes();
        }
    }

    /**
     * 简单的方法查找：优先匹配参数个数和可赋值关系
     */
    private Method findMethod(Class<?> clazz, String methodName, Object... args) {
        Class<?>[] argTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            argTypes[i] = args[i] == null ? Object.class : args[i].getClass();
        }
        Method best = null;
        for (Method m : clazz.getDeclaredMethods()) {
            if (!m.getName().equals(methodName)) {
                continue;
            }
            Class<?>[] paramTypes = m.getParameterTypes();
            if (paramTypes.length != argTypes.length) {
                continue;
            }
            boolean compatible = true;
            for (int i = 0; i < paramTypes.length; i++) {
                if (!isParameterCompatible(paramTypes[i], args[i], argTypes[i])) {
                    compatible = false;
                    break;
                }
            }
            if (compatible) {
                best = m;
                break;
            }
        }
        return best;
    }

    /**
     * 判断参数类型是否兼容，额外考虑基本类型与包装类型的对应关系
     */
    private boolean isParameterCompatible(Class<?> paramType, Object arg, Class<?> argType) {
        if (arg == null) {
            // null 只能赋给非基本类型
            return !paramType.isPrimitive();
        }
        if (paramType.isAssignableFrom(argType)) {
            return true;
        }
        // 处理基本类型 <-> 包装类型
        if (paramType.isPrimitive()) {
            Class<?> wrapper = primitiveToWrapper(paramType);
            return wrapper != null && wrapper.isAssignableFrom(argType);
        }
        if (argType.isPrimitive()) {
            Class<?> wrapper = primitiveToWrapper(argType);
            return wrapper != null && paramType.isAssignableFrom(wrapper);
        }
        return false;
    }

    private Class<?> primitiveToWrapper(Class<?> type) {
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == float.class) return Float.class;
        if (type == double.class) return Double.class;
        if (type == char.class) return Character.class;
        return null;
    }

    /**
     * 自定义 ClassLoader：优先从内存缓存加载类
     */
    private class CustomClassLoader extends ClassLoader {
        CustomClassLoader() {
            super(ClassLoader.getSystemClassLoader().getParent());
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            String path = name.replace('.', '/') + ".class";
            byte[] bytes = classCache.get(path);
            if (bytes != null) {
                return defineClass(name, bytes, 0, bytes.length);
            }
            return super.findClass(name);
        }
    }
}
