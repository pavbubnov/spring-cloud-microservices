package com.javastart.deposit.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ValidateException {

    private List<String> field;

    private OffsetDateTime time;

    private List<String> message;

}
