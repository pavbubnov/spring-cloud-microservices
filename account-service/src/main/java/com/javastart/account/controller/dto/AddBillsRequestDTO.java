package com.javastart.account.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class AddBillsRequestDTO {

    @JsonProperty("bills")
    private List<Long> bills;

}
