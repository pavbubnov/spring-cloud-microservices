package com.javastart.bill.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
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
