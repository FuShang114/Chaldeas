package com.example.demo.utils;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.reflect.Method;

/**
 * 在最小 JVM 中启动一个“轻量级” Spring 上下文，
 * 并通过 Spring 容器获取 Bean 来调用指定方法。
 *
 * 当前实现针对本项目的 Spring Boot 应用：{@code com.example.demo.DemoApplication}。
 * 后续可以通过配置 / profile 抽象出应用入口类。
 */
public class SpringContextInvoker {

    /**
     * 使用 Spring 容器获取目标 Bean 并调用方法
     *
     * @param meta 方法元数据（来自 DependencyAnalyzer）
     * @param args 调用参数
     */
    public static Object invokeWithSpringContext(DependencyAnalyzer.MethodMeta meta, Object... args) throws Exception {
        // 启动一个不包含 Web 容器的 Spring Boot 上下文，尽量“轻量化”
        ConfigurableApplicationContext context = new SpringApplicationBuilder(com.example.demo.DemoApplication.class)
                .web(WebApplicationType.NONE)
                .run();
        try {
            Class<?> targetClass = Class.forName(meta.getClassName());
            Object bean = context.getBean(targetClass);

            Method method;
            if (!meta.getParameterTypeNames().isEmpty()) {
                Class<?>[] paramTypes = new Class<?>[meta.getParameterTypeNames().size()];
                for (int i = 0; i < meta.getParameterTypeNames().size(); i++) {
                    paramTypes[i] = Class.forName(meta.getParameterTypeNames().get(i));
                }
                method = targetClass.getDeclaredMethod(meta.getMethodName(), paramTypes);
            } else {
                // 无签名信息时，简单按方法名 + 参数个数匹配
                method = findMethodByArgs(targetClass, meta.getMethodName(), args);
            }

            if (method == null) {
                throw new NoSuchMethodException("Method " + meta.getMethodName()
                        + " not found in class " + meta.getClassName());
            }

            method.setAccessible(true);
            return method.invoke(bean, args);
        } finally {
            // 对于一次性测试/调试，调用结束后直接关闭上下文，释放资源
            context.close();
        }
    }

    private static Method findMethodByArgs(Class<?> clazz, String methodName, Object... args) {
        Class<?>[] argTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            argTypes[i] = args[i] == null ? Object.class : args[i].getClass();
        }
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
                if (!isCompatible(paramTypes[i], args[i], argTypes[i])) {
                    compatible = false;
                    break;
                }
            }
            if (compatible) {
                return m;
            }
        }
        return null;
    }

    private static boolean isCompatible(Class<?> paramType, Object arg, Class<?> argType) {
        if (arg == null) {
            return !paramType.isPrimitive();
        }
        if (paramType.isAssignableFrom(argType)) {
            return true;
        }
        // 处理基本类型和包装类型
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

    private static Class<?> primitiveToWrapper(Class<?> type) {
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
}


