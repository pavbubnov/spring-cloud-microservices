package com.javastart.payment.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AccountResponseDTO {

    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("bills")
    private List<Long> bills;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("creation_date")
    private OffsetDateTime creationDate;

}
