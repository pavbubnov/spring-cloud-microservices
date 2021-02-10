package com.javastart.transfer.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Setter
@Getter
public class TransferRequestDTO {

    @JsonProperty("sender_bill_id")
    @Positive(message = "This value must be positive")
    private Long senderBillId;

    @JsonProperty("recipient_bill_id")
    @Positive(message = "This value must be positive")
    private Long recipientBillId;

    @JsonProperty("sender_account_id")
    @Positive(message = "This value must be positive")
    private Long senderAccountId;

    @JsonProperty("recipient_account_id")
    @Positive(message = "This value must be positive")
    private Long recipientAccountId;

    @JsonProperty("amount")
    @NotNull(message = "Enter amount")
    @Positive(message = "This value must be positive")
    private BigDecimal amount;

}
