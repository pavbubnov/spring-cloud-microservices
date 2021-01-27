package com.javastart.bill.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ValidateException {

    private List<String> message;

}
