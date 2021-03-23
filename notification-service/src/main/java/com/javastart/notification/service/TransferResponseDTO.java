package com.javastart.notification.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransferResponseDTO {

    @JsonProperty("sender_bill_id")
    private Long senderBillId;

    @JsonProperty("recipient_bill_id")
    private Long recipientBillId;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("email")
    private String email;

    @JsonProperty("available_amount")
    private BigDecimal availableAmount;

    @JsonProperty("is_plus")
    private Boolean isPlus;

}
