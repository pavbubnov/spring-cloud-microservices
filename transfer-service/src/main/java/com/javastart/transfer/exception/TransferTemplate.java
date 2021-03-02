package com.javastart.transfer.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class TransferTemplate {

    private String message;

    private OffsetDateTime time;

    private String exception;

}
