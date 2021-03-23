package com.javastart.payment.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class BillResponseDTO {

    @JsonProperty("bill_id")
    private Long billId;

    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("is_default")
    private Boolean isDefault;

    @JsonProperty("creation_date")
    private OffsetDateTime creationDate;

    @JsonProperty("is_overdraft_enabled")
    private Boolean overdraftEnabled;

}
