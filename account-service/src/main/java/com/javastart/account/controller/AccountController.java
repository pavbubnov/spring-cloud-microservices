package com.javastart.account.controller;

import com.javastart.account.controller.dto.AccountRequestDTO;
import com.javastart.account.controller.dto.AccountResponseDTO;
import com.javastart.account.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@Validated
public class AccountController {

    private final AccountService accountService;
    MethodArgumentNotValidException exception;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{accountId}")
    public AccountResponseDTO getAccount(@PathVariable Long accountId) {
        return new AccountResponseDTO(accountService.getAccountById(accountId));
    }

    @PostMapping("/")
    public Long createAccount(@Valid @RequestBody() AccountRequestDTO accountRequestDTO) {
        return accountService.createAccount(accountRequestDTO.getName(), accountRequestDTO.getEmail(),
                accountRequestDTO.getPhone(), accountRequestDTO.getBills());
    }

    @PutMapping("/{accountId}")
    public AccountResponseDTO updateAccount(@PathVariable Long accountId, @Valid
    @RequestBody AccountRequestDTO accountRequestDTO) {
        return new AccountResponseDTO(accountService.updateAccount(accountId, accountRequestDTO.getName(),
                accountRequestDTO.getEmail(), accountRequestDTO.getPhone(), accountRequestDTO.getBills()));
    }

    @PatchMapping("/{accountId}")
    public AccountResponseDTO addAccountBills(@PathVariable Long accountId,
                                              @RequestBody List<Long> additionalBill) {
        return new AccountResponseDTO(accountService.addAccountBills(accountId, additionalBill));
    }

    @DeleteMapping("/{accountId}")
    public String deleteAccount(@PathVariable Long accountId) {
        return new String(accountService.deleteAccount(accountId));
    }
}
