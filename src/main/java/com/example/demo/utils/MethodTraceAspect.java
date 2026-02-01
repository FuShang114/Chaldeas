package com.example.demo.utils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * 方法调用切面：
 * - 拦截项目中的业务方法（controller / service / repository）
 * - 在 StepRecorder 激活时，记录每一次方法调用的输入 / 输出 / 错误
 */
@Aspect
@Component
public class MethodTraceAspect {

    /**
     * 只关注业务层方法，避免把工具类自身也记录进去
     */
    @Around("execution(* com.example.demo.controller..*(..)) || " +
            "execution(* com.example.demo.service..*(..)) || " +
            "execution(* com.example.demo.repository..*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        if (!StepRecorder.isActive()) {
            return pjp.proceed();
        }

        String className = pjp.getSignature().getDeclaringTypeName();
        String methodName = pjp.getSignature().getName();
        Object[] args = pjp.getArgs();

        Instant start = Instant.now();
        Object result = null;
        String error = null;
        try {
            result = pjp.proceed();
            return result;
        } catch (Throwable t) {
            error = t.toString();
            throw t;
        } finally {
            Instant end = Instant.now();
            StepRecorder.record(className, methodName, args, result, error, start, end);
        }
    }
}


