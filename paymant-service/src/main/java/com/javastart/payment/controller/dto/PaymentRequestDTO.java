package com.javastart.payment.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Getter
@Setter
public class PaymentRequestDTO {

    @JsonProperty("accountId")
    @Positive(message = "This value must be positive")
    private Long accountId;

    @JsonProperty("billId")
    @Positive(message = "This value must be positive")
    private Long billId;

    @JsonProperty("amount")
    @NotNull(message = "Enter amount")
    @Positive(message = "This value must be positive")
    private BigDecimal amount;
}
