package com.javastart.notification.service;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DepositResponseDTO {

    private Long billId;

    private BigDecimal amount;

    private String mail;

    private BigDecimal availableAmount;
}
