package com.example.demo.infrastructure.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * @author youngbear
 * @email youngbear@aliyun.com
 * @date 2024-06-02 22:34
 * @blog <a href="https://blog.csdn.net/next_second">...</a>
 * @github <a href="https://github.com/YoungBear">...</a>
 * @description @AccessLog 注解的解析逻辑
 */
@Component
@Aspect
public class AccessLogAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessLogAspect.class);

    @Pointcut("@annotation(com.example.demo.infrastructure.aop.AccessLog)")
    private void accessLog() {
    }

    @Before("accessLog() && @annotation(accessLog)")
    public void doBefore(JoinPoint joinPoint,AccessLog accessLog) {
        System.out.println("注解作用的方法名: " + joinPoint.getSignature().getName());

        LOGGER.info("doBefore, methodName: {}, simpleName: {}, declaringType: {}, modifiers: {}.", joinPoint.getSignature().getName(),
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getDeclaringType(),
                Modifier.toString(joinPoint.getSignature().getModifiers())
        );
        LOGGER.info("doBefore, accessLog: {}", accessLog.value());
    }

    @After("accessLog() && @annotation(accessLog)")
    public void doAfter(AccessLog accessLog) {
        LOGGER.info("doAfter, accessLog: {}", accessLog.value());

    }

    @Around("accessLog()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();
        // 打印出参
        LOGGER.info("Response Args  : {}", result.toString());
        // 执行耗时
        LOGGER.info("Time-Consuming : {} ms", System.currentTimeMillis() - startTime);
        return result;
    }

    @AfterReturning(value = "accessLog()",returning = "methodResult")
    public Object doAfterReturning(JoinPoint joinPoint, Object methodResult) throws Throwable {
        // 修改返回值
        Map<String, String> result = (Map<String, String>) methodResult;
        result.put("testKey", "testValue");
        return result;
    }
}
