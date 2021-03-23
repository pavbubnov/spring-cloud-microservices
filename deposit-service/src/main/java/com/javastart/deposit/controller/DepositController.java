package com.javastart.deposit.controller;

import com.javastart.deposit.controller.dto.DepositRequestDTO;
import com.javastart.deposit.controller.dto.DepositResponseDTO;
import com.javastart.deposit.service.DepositService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Validated
public class DepositController {

    private final DepositService depositService;

    @Autowired
    public DepositController(DepositService depositService) {
        this.depositService = depositService;
    }

    @PostMapping("/deposits")
    public DepositResponseDTO deposit(@Valid @RequestBody DepositRequestDTO requestDTO) {
        return depositService.deposit(requestDTO.getAccountId(), requestDTO.getBillId(), requestDTO.getAmount());
    }

    @GetMapping("/deposits/{depositId}")
    public DepositResponseDTO getDeposit(@PathVariable @Positive(message = "Please, enter correct Id (Path)")
                                                 Long depositId) {
        return new DepositResponseDTO(depositService.getDepositById(depositId));
    }

    @GetMapping("deposits/bill/{billId}")
    public List<DepositResponseDTO> getDepositsByBillId(@PathVariable @Positive(message
            = "Please, enter correct Id (Path)") Long billId) {
        return depositService.getDepositsByBillId(billId).stream().
                map(DepositResponseDTO::new).
                collect(Collectors.toList());
    }

}
