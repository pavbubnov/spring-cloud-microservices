package com.javastart.notification.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentResponseDTO {

    @JsonProperty("bill_id")
    private Long billId;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("email")
    private String email;

    @JsonProperty("available_amount")
    private BigDecimal availableAmount;
}
