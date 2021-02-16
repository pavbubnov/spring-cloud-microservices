package com.javastart.transfer.controller;

import com.javastart.transfer.controller.dto.TransferRequestDTO;
import com.javastart.transfer.controller.dto.TransferResponseDTO;
import com.javastart.transfer.entity.Transfer;
import com.javastart.transfer.service.TransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@Validated
public class TransferController {

    private final TransferService transfeService;

    @Autowired
    public TransferController(TransferService transferService) {
        this.transfeService = transferService;
    }

    @PostMapping("/transfers")
    public TransferResponseDTO transfer(@Valid @RequestBody TransferRequestDTO transferRequestDTO) {
        return transfeService.transfer(transferRequestDTO);
    }

    @GetMapping("/transfers/{transferId}")
    public Transfer getTransferById(@PathVariable Long transferId) {
        return transfeService.getTransferById(transferId);
    }

    @GetMapping("/transfers/sender/{senderBillId}")
    public List<Transfer> getTransfersBySenderBillId(@PathVariable Long senderBillId) {
        return transfeService.getTransfersBySenderBillId(senderBillId);
    }

    @GetMapping("/transfers/recipient/{recipientBillId}")
    public List<Transfer> getTransfersByRecipientBillId(@PathVariable Long recipientBillId) {
        return transfeService.getTransfersByRecipientBillId(recipientBillId);
    }
}
