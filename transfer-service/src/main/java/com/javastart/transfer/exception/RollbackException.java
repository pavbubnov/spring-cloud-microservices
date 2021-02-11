package com.javastart.transfer.exception;

public class RollbackException extends RuntimeException {
    public RollbackException(String message) {
        super(message);
    }
}
