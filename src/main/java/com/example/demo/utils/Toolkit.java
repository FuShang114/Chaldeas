package com.example.demo.utils;

/**
 * 简单命令行入口，用于快速测试最小 JVM 启动能力
 */
public class Toolkit {

    public static void main(String[] args) throws Exception {
        String target = "com.example.demo.controller.UserController#getUserById(java.lang.Long)";
        Object input = 1L;

        DependencyAnalyzer analyzer = new DependencyAnalyzer();
        DependencyAnalyzer.AnalysisResult result = analyzer.analyze(target);

        // 对 Spring 管理的 Controller 方法：在最小 JVM 中启动一个轻量级 Spring 上下文，
        // 通过容器获取 Bean 再调用，避免 @Autowired 为空的问题
        Object out = SpringContextInvoker.invokeWithSpringContext(result.getMethodMeta(), input);

        // 如果是非 Spring 场景，可以退回到最小 JVM + 纯反射调用：
        // MinimalJVMStarter starter = new MinimalJVMStarter(result.getRequiredClassResources());
        // Object out = starter.invokeOnce(result.getMethodMeta(), input);

        System.out.println("Invoke result: " + out);
    }
}

