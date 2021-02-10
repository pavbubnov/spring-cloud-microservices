package com.javastart.bill.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class HandlerBillException {

    private String message;

    private OffsetDateTime time;

}
