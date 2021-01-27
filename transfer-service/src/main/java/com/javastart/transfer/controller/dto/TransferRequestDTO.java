package com.javastart.transfer.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Setter
@Getter
public class TransferRequestDTO {

    @JsonProperty("senderBillId")
    @Positive(message = "This value must be positive")
    private Long senderBillId;

    @JsonProperty("recipientBillId")
    @Positive(message = "This value must be positive")
    private Long recipientBillId;

    @JsonProperty("senderAccountId")
    @Positive(message = "This value must be positive")
    private Long senderAccountId;

    @JsonProperty("recipientAccountId")
    @Positive(message = "This value must be positive")
    private Long recipientAccountId;

    @JsonProperty("amount")
    @NotNull(message = "Enter amount")
    @Positive(message = "This value must be positive")
    private BigDecimal amount;

}
