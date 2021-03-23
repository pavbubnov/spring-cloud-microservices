package com.javastart.transfer.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class TransferTemplateList {

    private List<String> field;

    private OffsetDateTime time;

    private List<String> message;

}
