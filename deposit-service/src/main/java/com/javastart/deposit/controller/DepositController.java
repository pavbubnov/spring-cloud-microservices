package com.javastart.deposit.controller;

import com.javastart.deposit.controller.dto.DepositRequestDTO;
import com.javastart.deposit.controller.dto.DepositResponseDTO;
import com.javastart.deposit.service.DepositService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class DepositController {

    private final DepositService depositService;

    @Autowired
    public DepositController(DepositService depositService) {
        this.depositService = depositService;
    }

    @PostMapping("/deposits")
    public DepositResponseDTO deposit(@RequestBody DepositRequestDTO requestDTO) {
        return depositService.deposit(requestDTO.getAccountId(), requestDTO.getBillId(), requestDTO.getAmount());
    }

    @GetMapping("/deposits/{depositId}")
    public DepositResponseDTO getDeposit(@PathVariable Long depositId) {
        return new DepositResponseDTO(depositService.getDepositById(depositId));
    }

    @GetMapping("deposits/bill/{billId}")
    public List<DepositResponseDTO> getDepositsByBillId (@PathVariable Long billId) {
        return depositService.getDepositsByBillId(billId).stream().
                map(DepositResponseDTO::new).
                collect(Collectors.toList());
    }

}
