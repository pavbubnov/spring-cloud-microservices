package com.javastart.transfer.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class NotificationResponseDTO {

    private Long billId;

    private BigDecimal amount;

    private String mail;

    private BigDecimal availableAmount;
}
