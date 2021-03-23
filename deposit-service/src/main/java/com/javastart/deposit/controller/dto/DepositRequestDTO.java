package com.javastart.deposit.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Getter
@Setter
public class DepositRequestDTO {

    @JsonProperty("account_id")
    @Positive(message = "This value must be positive")
    private Long accountId;

    @JsonProperty("bill_id")
    @Positive(message = "This value must be positive")
    private Long billId;

    @JsonProperty("amount")
    @NotNull(message = "Enter amount")
    @Positive(message = "This value must be positive")
    private BigDecimal amount;
}
