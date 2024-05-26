package org.recipefinder.recipefinder.aop;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.recipefinder.recipefinder.auth.dto.RegisterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("execution(* org.recipefinder.recipefinder.auth.AuthenticationService.register(..))")
    public void logRegister(JoinPoint joinPoint) {
        LOGGER.info("Logging register");

        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof RegisterRequest registerRequest) {
                LOGGER.info("Register request: {}", registerRequest.email());
            }
        }
    }

    @Before("execution(* org.recipefinder.recipefinder.auth.AuthenticationService.login(..))")
    public void logLogin(JoinPoint joinPoint) {
        LOGGER.info("Logging login");

        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof RegisterRequest registerRequest) {
                LOGGER.info("Login attempt for user: {}", registerRequest.email());
            }
        }
    }
}
