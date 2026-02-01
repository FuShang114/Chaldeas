package com.example.demo.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 最小化依赖分析器（简化版本）
 *
 * 目标：
 * - 接收一个目标方法描述字符串：ClassName#methodName(参数全限定名,...)
 * - 解析出目标方法的元信息 {@link MethodMeta}
 * - 通过基于反射的类型遍历，推导出需要加载的最小类资源集合，用于最小 JVM 启动
 *
 * 注意：
 * - 这里不做字节码级调用图分析，只分析类型签名层面的依赖关系
 * - 后续可以在此基础上逐步增强
 */
public class DependencyAnalyzer {

    /**
     * 方法元信息
     */
    public static class MethodMeta {
        private final String className;
        private final String methodName;
        private final List<String> parameterTypeNames;

        public MethodMeta(String className, String methodName, List<String> parameterTypeNames) {
            this.className = className;
            this.methodName = methodName;
            this.parameterTypeNames = parameterTypeNames;
        }

        public String getClassName() {
            return className;
        }

        public String getMethodName() {
            return methodName;
        }

        public List<String> getParameterTypeNames() {
            return parameterTypeNames;
        }

        @Override
        public String toString() {
            return className + "#" + methodName + "(" + String.join(",", parameterTypeNames) + ")";
        }
    }

    /**
     * 依赖分析结果
     */
    public static class AnalysisResult {
        private final MethodMeta methodMeta;
        private final Set<String> requiredClassResources;

        public AnalysisResult(MethodMeta methodMeta, Set<String> requiredClassResources) {
            this.methodMeta = methodMeta;
            this.requiredClassResources = requiredClassResources;
        }

        public MethodMeta getMethodMeta() {
            return methodMeta;
        }

        /**
         * @return 类资源路径集合，例如 com/example/demo/controller/UserController.class
         */
        public Set<String> getRequiredClassResources() {
            return requiredClassResources;
        }
    }

    private final Set<String> analyzedClasses = new HashSet<>();
    private final Set<String> requiredResources = new HashSet<>();

    /**
     * 入口方法：解析目标字符串并执行依赖分析
     *
     * @param target 目标方法，格式示例：
     *               - com.example.demo.controller.UserController#getUserById(java.lang.Long)
     *               - com.example.demo.controller.UserController#getUserById
     *               - com.example.demo.DemoApplication#main(java.lang.String[])
     */
    public AnalysisResult analyze(String target) throws Exception {
        analyzedClasses.clear();
        requiredResources.clear();

        MethodMeta meta = parseTarget(target);

        Class<?> targetClass = Class.forName(meta.getClassName());
        analyzeClass(targetClass);

        // 可选：如果能精确解析方法签名，则再分析一次该方法的参数/返回/异常类型
        Method method = findMethod(targetClass, meta);
        if (method != null) {
            analyzeMethod(method);
        }

        return new AnalysisResult(meta, new HashSet<>(requiredResources));
    }

    /**
     * 解析目标字符串为 MethodMeta
     */
    private MethodMeta parseTarget(String target) {
        String[] parts = target.split("#", 2);
        String className = parts[0].trim();
        String methodPart = parts.length > 1 ? parts[1].trim() : "main";

        String methodName;
        List<String> paramTypeNames = new ArrayList<>();

        int left = methodPart.indexOf('(');
        int right = methodPart.lastIndexOf(')');
        if (left == -1 || right == -1) {
            // 没有参数列表
            methodName = methodPart;
        } else {
            methodName = methodPart.substring(0, left).trim();
            String paramList = methodPart.substring(left + 1, right).trim();
            if (!paramList.isEmpty()) {
                for (String s : paramList.split(",")) {
                    String t = s.trim();
                    if (!t.isEmpty()) {
                        paramTypeNames.add(t);
                    }
                }
            }
        }

        return new MethodMeta(className, methodName, paramTypeNames);
    }

    /**
     * 基于 MethodMeta 在 Class 中查找具体 Method
     */
    private Method findMethod(Class<?> clazz, MethodMeta meta) {
        try {
            if (!meta.getParameterTypeNames().isEmpty()) {
                Class<?>[] paramTypes = new Class<?>[meta.getParameterTypeNames().size()];
                for (int i = 0; i < meta.getParameterTypeNames().size(); i++) {
                    paramTypes[i] = Class.forName(meta.getParameterTypeNames().get(i));
                }
                return clazz.getDeclaredMethod(meta.getMethodName(), paramTypes);
            }

            // 没有给出参数类型时，优先尝试无参方法
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.getName().equals(meta.getMethodName())) {
                    return m;
                }
            }
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
        }
        return null;
    }

    /**
     * 递归分析类及其关联类型
     */
    private void analyzeClass(Class<?> clazz) {
        if (clazz == null || analyzedClasses.contains(clazz.getName())) {
            return;
        }

        analyzedClasses.add(clazz.getName());
        requiredResources.add(clazz.getName().replace('.', '/') + ".class");

        // 父类
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            analyzeClass(superClass);
        }

        // 接口
        for (Class<?> iface : clazz.getInterfaces()) {
            analyzeClass(iface);
        }

        // 非静态字段类型
        Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .forEach(f -> analyzeType(f.getType()));

        // 构造方法参数/异常
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            analyzeMethodParameters(constructor.getParameterTypes());
            analyzeMethodExceptions(constructor.getExceptionTypes());
        }

        // 非静态方法参数/返回/异常
        for (Method method : clazz.getDeclaredMethods()) {
            if (!Modifier.isStatic(method.getModifiers())) {
                analyzeMethod(method);
            }
        }
    }

    private void analyzeMethod(Method method) {
        analyzeMethodParameters(method.getParameterTypes());
        analyzeType(method.getReturnType());
        analyzeMethodExceptions(method.getExceptionTypes());
    }

    private void analyzeMethodParameters(Class<?>[] parameterTypes) {
        for (Class<?> paramType : parameterTypes) {
            analyzeType(paramType);
        }
    }

    private void analyzeMethodExceptions(Class<?>[] exceptionTypes) {
        for (Class<?> exceptionType : exceptionTypes) {
            analyzeType(exceptionType);
        }
    }

    /**
     * 类型级别依赖分析（简化版）
     */
    private void analyzeType(Class<?> type) {
        if (type == null) {
            return;
        }
        if (type.isPrimitive() || type == String.class || type == void.class) {
            return;
        }
        if (type.isArray()) {
            analyzeType(type.getComponentType());
            return;
        }
        if (Collection.class.isAssignableFrom(type)) {
            // 泛型信息在运行时丢失，这里先不做更复杂分析
            return;
        }
        analyzeClass(type);
    }
}
