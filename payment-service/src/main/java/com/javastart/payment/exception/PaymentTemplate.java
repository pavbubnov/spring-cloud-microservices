package com.javastart.payment.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class PaymentTemplate {

    private String message;

    private OffsetDateTime time;

    private String exception;
}
