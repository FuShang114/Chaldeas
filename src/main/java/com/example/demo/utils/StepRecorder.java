package com.example.demo.utils;

import com.example.demo.utils.DebugModels.MethodCallRecord;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 线程级别的方法调用记录器
 *
 * - 通过 ThreadLocal 在一次调试会话内记录方法调用链路
 * - 由 AOP 切面在方法前后填充数据
 * - 由 DebugSessionManager 显式 start()/endAndGet() 控制生命周期
 */
public class StepRecorder {

    private static final ThreadLocal<List<MethodCallRecord>> RECORDS = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> ACTIVE = ThreadLocal.withInitial(() -> Boolean.FALSE);

    public static void start() {
        ACTIVE.set(Boolean.TRUE);
        RECORDS.set(new ArrayList<>());
    }

    public static List<MethodCallRecord> endAndGet() {
        ACTIVE.set(Boolean.FALSE);
        List<MethodCallRecord> list = RECORDS.get();
        RECORDS.remove();
        return list != null ? list : new ArrayList<>();
    }

    public static boolean isActive() {
        return Boolean.TRUE.equals(ACTIVE.get());
    }

    /**
     * 供 AOP 切面调用：记录一次方法调用
     */
    public static void record(String className,
                              String methodName,
                              Object[] args,
                              Object returnValue,
                              String error,
                              Instant start,
                              Instant end) {
        if (!isActive()) {
            return;
        }
        List<MethodCallRecord> list = RECORDS.get();
        if (list == null) {
            list = new ArrayList<>();
            RECORDS.set(list);
        }
        MethodCallRecord rec = new MethodCallRecord();
        rec.setClassName(className);
        rec.setMethodName(methodName);
        rec.setArgs(args);
        rec.setReturnValue(returnValue);
        rec.setError(error);
        rec.setStartTime(start);
        rec.setEndTime(end);
        if (start != null && end != null) {
            rec.setDurationMillis(java.time.Duration.between(start, end).toMillis());
        }
        list.add(rec);
    }
}


