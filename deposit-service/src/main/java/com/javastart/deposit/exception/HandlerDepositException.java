package com.javastart.deposit.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class HandlerDepositException {

    private String message;

    private OffsetDateTime time;

}
