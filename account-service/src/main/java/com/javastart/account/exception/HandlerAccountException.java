package com.javastart.account.exception;

import com.sun.scenario.effect.Offset;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class HandlerAccountException {

    private String message;

    private OffsetDateTime time;

}
