package com.javastart.transfer.controller.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Setter
@Getter
public class TransferRequestDTO {

    private Long senderBillId;

    private Long recipientBillId;

    private Long senderAccountId;

    private Long recipientAccountId;

    private BigDecimal amount;

}
