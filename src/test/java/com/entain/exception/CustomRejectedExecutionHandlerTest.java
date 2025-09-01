package com.entain.exception;

import org.springframework.boot.test.context.SpringBootTest;

import static com.entain.config.EntainConstant.TASK_REJECTED;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


@SpringBootTest
class CustomRejectedExecutionHandlerTest {

    private CustomRejectedExecutionHandler handler;
    private ThreadPoolExecutor mockExecutor;
    private GlobalExceptionHandler exceptionHandler;

    private static final String DUMMY_TASK = "Dummy task";

    @BeforeEach
    void setup() {
        handler = new CustomRejectedExecutionHandler();

        mockExecutor = new ThreadPoolExecutor(
                1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1)
        );

        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testRejectedExecution_ThrowsException() {
        Runnable dummyTask = () -> System.out.println(DUMMY_TASK);

        mockExecutor.execute(dummyTask);
        mockExecutor.execute(dummyTask);

        AsyncTaskRejectedException exception = assertThrows(
                AsyncTaskRejectedException.class,
                () -> handler.rejectedExecution(dummyTask, mockExecutor)
        );

        assertThat(exception.getMessage()).contains(TASK_REJECTED);
    }
}