package com.javastart.deposit.controller.dto;

import com.javastart.deposit.entity.Deposit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepositResponseDTO {

    private Long billId;

    private BigDecimal amount;

    private String mail;

    private BigDecimal availableAmount;

    public DepositResponseDTO (Deposit deposit) {
        billId = deposit.getBillId();
        amount = deposit.getAmount();
        mail = deposit.getEmail();
        availableAmount = deposit.getAvailableAmount();
    }
}
