package com.javastart.transfer.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.javastart.transfer.entity.Transfer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponseDTO {

    @JsonProperty("sender_bill_id")
    private Long senderBillId;

    @JsonProperty("recipient_bill_id")
    private Long recipientBillId;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("creation_date")
    private OffsetDateTime creationDate;

    @JsonProperty("sender_email")
    private String senderEmail;

    @JsonProperty("recipient_email")
    private String recipientEmail;


    public TransferResponseDTO (Transfer transfer) {
        senderBillId = transfer.getSenderBillId();
        recipientBillId = transfer.getRecipientBillId();
        amount = transfer.getAmount();
        creationDate = transfer.getCreationDate();
        senderEmail = transfer.getSenderEmail();
        recipientEmail = transfer.getRecipientEmail();
    }

}
