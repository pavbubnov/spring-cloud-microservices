package com.javastart.payment.controller.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentRequestDTO {

    private Long accountId;

    private Long billId;

    private BigDecimal amount;
}
