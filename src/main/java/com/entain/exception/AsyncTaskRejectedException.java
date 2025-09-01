package com.entain.exception;

public class AsyncTaskRejectedException extends RuntimeException {
    public AsyncTaskRejectedException(String message) {
        super(message);
    }
}