package com.javastart.deposit.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("bill_id")
    private Long billId;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("email")
    private String email;

    @JsonProperty("available_amount")
    private BigDecimal availableAmount;

    public DepositResponseDTO(Deposit deposit) {
        billId = deposit.getBillId();
        amount = deposit.getAmount();
        email = deposit.getEmail();
        availableAmount = deposit.getAvailableAmount();
    }
}
