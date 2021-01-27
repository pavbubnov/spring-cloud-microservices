package com.javastart.account.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class AccountRequestDTO {

    @JsonProperty("name")
    @NotNull(message = "Please, enter your name")
    private String name;

    @JsonProperty("email")
    @NotNull(message = "Please, enter your email")
    @Pattern(regexp = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$", message = "Please, enter correct email")
    private String email;

    @JsonProperty("phone")
    @NotNull(message = "Please, enter your phone")
    private String phone;

    @JsonProperty("bills")
    private List<Long> bills;

    private OffsetDateTime creationDate;
}
