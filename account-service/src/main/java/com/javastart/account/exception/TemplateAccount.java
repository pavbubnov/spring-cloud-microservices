package com.javastart.account.exception;

import com.sun.scenario.effect.Offset;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class TemplateAccount {

    private String message;

    private OffsetDateTime time;

    private String exception;

}
