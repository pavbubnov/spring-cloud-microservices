package com.javastart.transfer.controller.dto;

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

    private Long senderBillId;

    private Long recipientBillId;

    private BigDecimal amount;

    private OffsetDateTime creationDate;

    private String senderEmail;

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
