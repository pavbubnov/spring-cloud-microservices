package com.javastart.payment.controller;

import com.javastart.payment.controller.dto.PaymentRequestDTO;
import com.javastart.payment.controller.dto.PaymentResponseDTO;
import com.javastart.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments")
    public PaymentResponseDTO payment(@Valid @RequestBody PaymentRequestDTO requestDTO) {
        return paymentService.payment(requestDTO.getAccountId(), requestDTO.getBillId(), requestDTO.getAmount());
    }

    @GetMapping("/payments/{paymentId}")
    public PaymentResponseDTO getPayment(@PathVariable Long paymentId) {
        return new PaymentResponseDTO(paymentService.getPaymentById(paymentId));
    }

    @GetMapping("payments/bill/{billId}")
    public List<PaymentResponseDTO> getPaymentsByBillId(@PathVariable Long billId) {
        return paymentService.getPaymentsByBillId(billId).stream().
                map(PaymentResponseDTO::new).
                collect(Collectors.toList());
    }
}
