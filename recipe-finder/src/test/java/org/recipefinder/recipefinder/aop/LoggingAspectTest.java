package org.recipefinder.recipefinder.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.recipefinder.recipefinder.auth.dto.RegisterRequest;

import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggingAspectTest {

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @InjectMocks
    private LoggingAspect underTest;

    @Mock
    private Logger LOGGER;

    @Test
    void should_produce_logging_when_user_registers() throws Throwable {
        //given
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = System.out;
        System.setOut(new PrintStream(baos));
        RegisterRequest r = new RegisterRequest("name", "lastname", "email", "pw");
        Object[] args = new Object[] {r};
        //when
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        underTest.logRegister(proceedingJoinPoint);
        System.setOut(ps);
        //then
        String logOutput = baos.toString();
        assertThat(logOutput).contains("Logging register")
                             .contains("Register request: email");
    }

    @Test
    void should_produce_logging_when_costumer_performs_login() {
        //given
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = System.out;
        System.setOut(new PrintStream(baos));
        RegisterRequest r = new RegisterRequest("name", "lastname", "email", "pw");
        Object[] args = new Object[] {r};
        //when
        when(proceedingJoinPoint.getArgs()).thenReturn(args);
        underTest.logLogin(proceedingJoinPoint);
        System.setOut(ps);
        //then
        String logOutput = baos.toString();
        assertThat(logOutput).contains("Logging login")
                             .contains("Login attempt for user: email");
    }
}
