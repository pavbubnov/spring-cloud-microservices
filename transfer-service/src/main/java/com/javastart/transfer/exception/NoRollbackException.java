package com.javastart.transfer.exception;

public class NoRollbackException extends RuntimeException{

    public NoRollbackException(String message) {
        super(message);
    }
}
