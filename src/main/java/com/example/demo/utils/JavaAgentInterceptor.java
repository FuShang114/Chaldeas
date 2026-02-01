package com.example.demo.utils;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Java探针拦截器
 * 用于拦截项目方法的输出，包括SQL、网络IO的输出对象
 */
public class JavaAgentInterceptor {

    public static final String AGENT_ARGS = "agentArgs";
    private static Instrumentation instrumentation;
    private static Map<String, Boolean> projectClasses = new ConcurrentHashMap<>();
    private static Map<String, MethodCall> methodCalls = new ConcurrentHashMap<>();
    private static List<String> projectPackages = new ArrayList<>();

    /**
     * Java Agent入口方法
     * @param agentArgs 代理参数
     * @param inst  instrumentation实例
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        instrumentation = inst;
        
        // 解析代理参数
        if (agentArgs != null && !agentArgs.isEmpty()) {
            String[] args = agentArgs.split(",");
            for (String arg : args) {
                if (arg.startsWith("packages=")) {
                    String packages = arg.substring(9);
                    for (String pkg : packages.split("\\|")) {
                        projectPackages.add(pkg);
                    }
                }
            }
        }
        
        // 注册类转换器
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, 
                                   ProtectionDomain protectionDomain, byte[] classfileBuffer) {
                // 转换类名格式
                String javaClassName = className.replace('/', '.');
                
                // 检查是否是本项目的类
                if (isProjectClass(javaClassName)) {
                    // 这里使用ASM或Javassist进行字节码转换
                    // 简化实现，直接返回原始字节码
                    // 实际项目中需要使用字节码操作库添加拦截逻辑
                    return classfileBuffer;
                }
                
                return classfileBuffer;
            }
        });
    }

    /**
     * AgentMain入口方法
     * @param agentArgs 代理参数
     * @param inst  instrumentation实例
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        premain(agentArgs, inst);
    }

    /**
     * 检查是否是本项目的类
     * @param className 类名
     * @return 是否是本项目的类
     */
    private static boolean isProjectClass(String className) {
        // 缓存检查结果
        if (projectClasses.containsKey(className)) {
            return projectClasses.get(className);
        }
        
        // 检查是否在项目包列表中
        for (String pkg : projectPackages) {
            if (className.startsWith(pkg)) {
                projectClasses.put(className, true);
                return true;
            }
        }
        
        projectClasses.put(className, false);
        return false;
    }

    /**
     * 记录方法调用
     * @param className 类名
     * @param methodName 方法名
     * @param input 输入参数
     * @param output 输出结果
     */
    public static void recordMethodCall(String className, String methodName, Object[] input, Object output) {
        String key = className + "." + methodName;
        MethodCall methodCall = new MethodCall();
        methodCall.setClassName(className);
        methodCall.setMethodName(methodName);
        methodCall.setInput(input);
        methodCall.setOutput(output);
        methodCall.setTimestamp(System.currentTimeMillis());
        
        methodCalls.put(key, methodCall);
    }

    /**
     * 获取方法调用记录
     * @return 方法调用记录
     */
    public static Map<String, MethodCall> getMethodCalls() {
        return methodCalls;
    }

    /**
     * 清空方法调用记录
     */
    public static void clearMethodCalls() {
        methodCalls.clear();
    }

    /**
     * 添加项目包
     * @param pkg 包名
     */
    public static void addProjectPackage(String pkg) {
        projectPackages.add(pkg);
    }

    /**
     * 获取Instrumentation实例
     * @return Instrumentation实例
     */
    public static Instrumentation getInstrumentation() {
        return instrumentation;
    }

    /**
     * 方法调用记录类
     */
    public static class MethodCall {
        private String className;
        private String methodName;
        private Object[] input;
        private Object output;
        private long timestamp;
        private List<MethodCall> children = new ArrayList<>();

        // Getters and Setters
        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public Object[] getInput() {
            return input;
        }

        public void setInput(Object[] input) {
            this.input = input;
        }

        public Object getOutput() {
            return output;
        }

        public void setOutput(Object output) {
            this.output = output;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public List<MethodCall> getChildren() {
            return children;
        }

        public void addChild(MethodCall child) {
            this.children.add(child);
        }
    }

    /**
     * 方法拦截器接口
     */
    public interface MethodInterceptor {
        /**
         * 方法执行前
         * @param className 类名
         * @param methodName 方法名
         * @param input 输入参数
         */
        void beforeMethod(String className, String methodName, Object[] input);

        /**
         * 方法执行后
         * @param className 类名
         * @param methodName 方法名
         * @param input 输入参数
         * @param output 输出结果
         */
        void afterMethod(String className, String methodName, Object[] input, Object output);

        /**
         * 方法执行异常
         * @param className 类名
         * @param methodName 方法名
         * @param input 输入参数
         * @param throwable 异常
         */
        void onException(String className, String methodName, Object[] input, Throwable throwable);
    }

    /**
     * 默认方法拦截器
     */
    public static class DefaultMethodInterceptor implements MethodInterceptor {
        @Override
        public void beforeMethod(String className, String methodName, Object[] input) {
            // 方法执行前的处理
        }

        @Override
        public void afterMethod(String className, String methodName, Object[] input, Object output) {
            // 记录方法调用
            recordMethodCall(className, methodName, input, output);
        }

        @Override
        public void onException(String className, String methodName, Object[] input, Throwable throwable) {
            // 异常处理
        }
    }
}
