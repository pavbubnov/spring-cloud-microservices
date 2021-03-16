package com.javastart.payment.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.javastart.payment.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {

    @JsonProperty("bill_id")
    private Long billId;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("email")
    private String email;

    @JsonProperty("available_amount")
    private BigDecimal availableAmount;

    public PaymentResponseDTO(Payment payment) {
        billId = payment.getBillId();
        amount = payment.getAmount();
        email = payment.getEmail();
        availableAmount = payment.getAvailableAmount();
    }
}
