package com.javastart.account.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.javastart.account.entity.Account;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class AccountResponseDTO {

    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("creation_date")
    private OffsetDateTime creationDate;

    @JsonProperty("bills")
    private List<Long> bills;

    public AccountResponseDTO(Account account) {
        accountId = account.getAccountId();
        name = account.getName();
        email = account.getEmail();
        phone = account.getPhone();
        bills = account.getBills();
        creationDate = account.getCreationDate();
    }
}
