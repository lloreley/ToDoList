package com.vlad.todo.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;    
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("execution(* com.vlad.todo..*(..))")
    public void logBefore(JoinPoint joinPoint) {
        if (logger.isDebugEnabled()) {
            logger.info("Выполняется: {}", joinPoint.getSignature().toShortString());
        }
    }

    @AfterReturning(pointcut = "execution(* com.vlad.todo..*(..))",
            returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        if (logger.isDebugEnabled()) {
            logger.info("Выполняется: {} с результатом: {}",
                    joinPoint.getSignature().toShortString(), result);
        }
    }

    @AfterThrowing(pointcut = "execution(* com.vlad.todo..*(..))", throwing = "error")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable error) {
        if (logger.isDebugEnabled()) {
            logger.error("Исключение в: {} с причиной: {}",
                    joinPoint.getSignature().toShortString(), error.getMessage());
        }
    }
}