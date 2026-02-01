package com.example.demo.utils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 调试相关的基础模型定义
 *
 * 第一阶段：仅做一次性执行 + 回放型步骤
 * - 一个会话只包含一条根方法调用记录，方便前端先打通链路
 * - 预留字段，后续可承载完整调用树 / SQL / HTTP 事件等
 */
public class DebugModels {

    /**
     * 方法来源类型
     */
    public enum SourceType {
        PROJECT,
        EXTERNAL
    }

    /**
     * 单次方法调用记录（当前只记录根方法）
     */
    public static class MethodCallRecord {
        private final String id = UUID.randomUUID().toString();
        private String className;
        private String methodName;
        private SourceType sourceType = SourceType.PROJECT;
        private Object[] args;
        private Object returnValue;
        private String error;
        private Instant startTime;
        private Instant endTime;
        private Long durationMillis;

        public String getId() {
            return id;
        }

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

        public SourceType getSourceType() {
            return sourceType;
        }

        public void setSourceType(SourceType sourceType) {
            this.sourceType = sourceType;
        }

        public Object[] getArgs() {
            return args;
        }

        public void setArgs(Object[] args) {
            this.args = args;
        }

        public Object getReturnValue() {
            return returnValue;
        }

        public void setReturnValue(Object returnValue) {
            this.returnValue = returnValue;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public Instant getStartTime() {
            return startTime;
        }

        public void setStartTime(Instant startTime) {
            this.startTime = startTime;
        }

        public Instant getEndTime() {
            return endTime;
        }

        public void setEndTime(Instant endTime) {
            this.endTime = endTime;
        }

        public Long getDurationMillis() {
            return durationMillis;
        }

        public void setDurationMillis(Long durationMillis) {
            this.durationMillis = durationMillis;
        }
    }

    /**
     * 调试会话
     *
     * 第一阶段：只包含一条根方法调用记录，steps 数组长度为 1
     */
    public static class DebugSession {
        private final String id = UUID.randomUUID().toString();
        private String targetMethod; // 原始字符串
        private MethodCallRecord rootCall;
        private final List<MethodCallRecord> steps = new ArrayList<>();
        private int currentStepIndex = -1;

        public String getId() {
            return id;
        }

        public String getTargetMethod() {
            return targetMethod;
        }

        public void setTargetMethod(String targetMethod) {
            this.targetMethod = targetMethod;
        }

        public MethodCallRecord getRootCall() {
            return rootCall;
        }

        public void setRootCall(MethodCallRecord rootCall) {
            this.rootCall = rootCall;
        }

        public List<MethodCallRecord> getSteps() {
            return steps;
        }

        public int getCurrentStepIndex() {
            return currentStepIndex;
        }

        public void setCurrentStepIndex(int currentStepIndex) {
            this.currentStepIndex = currentStepIndex;
        }
    }
}


