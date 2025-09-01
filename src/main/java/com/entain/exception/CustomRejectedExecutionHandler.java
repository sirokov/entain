package com.entain.exception;

import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import static com.entain.config.EntainConstant.QUEUE_SIZE;
import static com.entain.config.EntainConstant.THREAD_POOL_FULL_ERROR;

@Slf4j
public class CustomRejectedExecutionHandler implements RejectedExecutionHandler {

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        String message = THREAD_POOL_FULL_ERROR
                + executor.getActiveCount() 
                + QUEUE_SIZE + executor.getQueue().size();
        log.error(message);
        throw new AsyncTaskRejectedException(message);
    }
}