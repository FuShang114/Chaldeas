package com.example.demo.utils;

import com.example.demo.utils.DebugModels.DebugSession;
import com.example.demo.utils.DebugModels.MethodCallRecord;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 调试会话管理器（第一阶段：单步回放）
 *
 * 设计目标：
 * - 每次 /api/debug/start 会创建一个新的 DebugSession
 * - 当前实现只执行一次根方法，并生成一个包含单一步骤的会话
 * - /api/debug/next 只是返回这一步骤，便于前端打通交互
 *
 * 后续可以在此基础上扩展为完整调用树 + 多步回放
 */
public class DebugSessionManager {

    private static final Map<String, DebugSession> SESSIONS = new ConcurrentHashMap<>();

    private final DependencyAnalyzer dependencyAnalyzer = new DependencyAnalyzer();

    /**
     * 启动一次新的调试会话：
     * - 分析目标方法
     * - 选择合适的调用策略（Spring 容器 / 最小 JVM 反射）
     * - 记录调用链路（方法级步骤）
     *
     * @param targetMethod 目标方法字符串
     * @param input        输入对象（JSON 已在外层解析为 Map 或其它结构，这里当作一个参数传入）
     */
    public DebugSession startSession(String targetMethod, Object input) throws Exception {
        DependencyAnalyzer.AnalysisResult analysisResult = dependencyAnalyzer.analyze(targetMethod);
        DependencyAnalyzer.MethodMeta meta = analysisResult.getMethodMeta();

        String className = meta.getClassName();
        String methodName = meta.getMethodName();

        // 将 JSON 反序列化后的输入，尽量转换为方法参数需要的 Java 类型
        Object[] args = convertArgs(meta, input);

        // 启动本次会话的步骤记录（AOP 只在激活时记录）
        StepRecorder.start();
        MethodCallRecord entryRecord = new MethodCallRecord();
        entryRecord.setClassName(className);
        entryRecord.setMethodName(methodName);
        entryRecord.setArgs(args);
        Instant entryStart = Instant.now();
        entryRecord.setStartTime(entryStart);

        try {
            Object result;
            if (isSpringManaged(meta.getClassName())) {
                // Spring 管理的 Bean：在当前进程内启动/复用轻量 Spring 上下文，通过容器获取 Bean 再调用
                result = SpringContextInvoker.invokeWithSpringContext(meta, args);
            } else {
                // 非 Spring 场景：退回到最小 JVM + 反射调用
                MinimalJVMStarter starter = new MinimalJVMStarter(analysisResult.getRequiredClassResources());
                result = starter.invokeOnce(meta, args);
            }
            entryRecord.setReturnValue(result);
        } catch (Throwable t) {
            Throwable cause = t.getCause() != null ? t.getCause() : t;
            entryRecord.setError(cause.toString());
        } finally {
            Instant end = Instant.now();
            entryRecord.setEndTime(end);
            entryRecord.setDurationMillis(java.time.Duration.between(entryStart, end).toMillis());
        }

        // 获取本次会话中 AOP 记录的完整方法调用链
        java.util.List<MethodCallRecord> steps = StepRecorder.endAndGet();

        DebugSession session = new DebugSession();
        session.setTargetMethod(targetMethod);
        session.setRootCall(entryRecord);

        // 将入口步骤放在第一位，后面跟随 AOP 记录的链路（如果入口方法本身也被 AOP 记录到，可按需去重）
        session.getSteps().add(entryRecord);
        session.getSteps().addAll(steps);
        session.setCurrentStepIndex(0);

        SESSIONS.put(session.getId(), session);
        return session;
    }

    public DebugSession getSession(String sessionId) {
        return SESSIONS.get(sessionId);
    }

    /**
     * 判断目标类是否是 Spring 管理的 Bean（简单启发式）
     */
    private boolean isSpringManaged(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            return clazz.isAnnotationPresent(RestController.class)
                    || clazz.isAnnotationPresent(Controller.class)
                    || clazz.isAnnotationPresent(Service.class)
                    || clazz.isAnnotationPresent(Repository.class);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * 将 JSON 解析出来的 input 转换为方法需要的参数类型
     * 当前阶段只支持“单参数”场景，后续可以根据 MethodMeta 的参数个数扩展为多参数
     */
    private Object[] convertArgs(DependencyAnalyzer.MethodMeta meta, Object input) throws ClassNotFoundException {
        if (meta.getParameterTypeNames().isEmpty()) {
            return input == null ? new Object[0] : new Object[]{input};
        }
        // 目前只处理单参数方法
        String typeName = meta.getParameterTypeNames().get(0);
        Class<?> paramType = Class.forName(typeName);
        Object converted = convertSingleArg(paramType, input);
        return new Object[]{converted};
    }

    private Object convertSingleArg(Class<?> targetType, Object value) {
        if (value == null) {
            return null;
        }
        if (targetType.isInstance(value)) {
            return value;
        }
        // Number -> 各种数字类型
        if (value instanceof Number number) {
            if (targetType == Long.class || targetType == long.class) {
                return number.longValue();
            }
            if (targetType == Integer.class || targetType == int.class) {
                return number.intValue();
            }
            if (targetType == Double.class || targetType == double.class) {
                return number.doubleValue();
            }
            if (targetType == Float.class || targetType == float.class) {
                return number.floatValue();
            }
            if (targetType == Short.class || targetType == short.class) {
                return number.shortValue();
            }
            if (targetType == Byte.class || targetType == byte.class) {
                return number.byteValue();
            }
        }
        // 目标是 String
        if (targetType == String.class) {
            return String.valueOf(value);
        }
        // Enum 支持：input 为字符串时按名称匹配
        if (targetType.isEnum() && value instanceof String s) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            Object e = Enum.valueOf((Class<? extends Enum>) targetType, s);
            return e;
        }
        // 简化处理：其他情况直接返回原值，交给反射层去做进一步校验
        return value;
    }
}