package com.javastart.deposit.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class DepositTemplate {

    private String message;

    private OffsetDateTime time;

    private String exception;

}
