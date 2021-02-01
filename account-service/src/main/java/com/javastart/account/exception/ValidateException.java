package com.javastart.account.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@AllArgsConstructor
@Getter //добавил
public class ValidateException {

    private List<String> message;

}
