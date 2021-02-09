package com.javastart.bill.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
public class BillRequestDTO {

    @JsonProperty("account_id")
    @NotNull(message = "Enter account id")
    @Positive(message = "This value must be positive")
    private Long accountId;

    @JsonProperty("bill_id")
    @Positive(message = "This value must be positive")
    private Long billId;

    @JsonProperty("amount")
    @NotNull(message = "Enter amount")
    @PositiveOrZero(message = "This value must be positive or 0")
    private BigDecimal amount;

    @JsonProperty("is_default")
    @NotNull(message = "Do you want to set this bill default or not")
    private Boolean isDefault;

    @JsonProperty("creation_date")
    private OffsetDateTime creationDate;

    @JsonProperty("is_overdraft_enabled")
    @NotNull(message = "Do you want to enable overdraft or not")
    private Boolean overdraftEnabled;
}
