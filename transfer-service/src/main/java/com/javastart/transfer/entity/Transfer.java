package com.javastart.transfer.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long transferId;

    private Long senderBillId;

    private Long recipientBillId;

    private BigDecimal amount;

    private OffsetDateTime creationDate;

    private String senderEmail;

    private String recipientEmail;

    public Transfer(Long senderBillId, Long recipientBillId, BigDecimal amount, OffsetDateTime creationDate,
                    String senderEmail, String recipientEmail) {
        this.senderBillId = senderBillId;
        this.recipientBillId = recipientBillId;
        this.amount = amount;
        this.creationDate = creationDate;
        this.senderEmail = senderEmail;
        this.recipientEmail = recipientEmail;
    }
}
