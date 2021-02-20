package com.javastart.bill.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.javastart.bill.entity.Bill;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Data
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

    public BillResponseDTO(Bill bill) {
        billId = bill.getBillId();
        accountId = bill.getAccountId();
        amount = bill.getAmount();
        isDefault = bill.getIsDefault();
        creationDate = bill.getCreationDate();
        overdraftEnabled = bill.getOverdraftEnabled();
    }
}
