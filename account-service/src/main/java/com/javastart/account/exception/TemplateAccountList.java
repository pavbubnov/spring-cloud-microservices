package com.javastart.account.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Getter
public class TemplateAccountList {

    private List<String> field;

    private OffsetDateTime time;

    private List<String> message;


}
