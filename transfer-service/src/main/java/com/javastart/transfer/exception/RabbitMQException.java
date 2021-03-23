package com.javastart.transfer.exception;

import com.javastart.transfer.controller.dto.NotificationResponseDTO;

public class RabbitMQException extends RuntimeException {
    public RabbitMQException(String message) {
        super(message);
    }
}
