package com.javastart.transfer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javastart.transfer.action.ActionManager;
import com.javastart.transfer.controller.dto.NotificationResponseDTO;
import com.javastart.transfer.controller.dto.TransferRequestDTO;
import com.javastart.transfer.controller.dto.TransferResponseDTO;
import com.javastart.transfer.entity.Transfer;
import com.javastart.transfer.exception.TransferServiceException;
import com.javastart.transfer.repository.TransferRepository;
import com.javastart.transfer.rest.*;
import feign.FeignException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class TransferService {

    private static final String TOPIC_EXCHANGE_PAYMENT = "js.payment.notify.exchange";
    private static final String ROUTING_KEY_PAYMENT = "js.key.payment";
    private static final String TOPIC_EXCHANGE_DEPOSIT = "js.deposit.notify.exchange";
    private static final String ROUTING_KEY_DEPOSIT = "js.key.deposit";

    private final TransferRepository transferRepository;

    private final AccountServiceClient accountServiceClient;

    private final BillServiceClient billServiceClient;

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public TransferService(TransferRepository transferRepository, AccountServiceClient accountServiceClient, BillServiceClient billServiceClient, RabbitTemplate rabbitTemplate) {
        this.transferRepository = transferRepository;
        this.accountServiceClient = accountServiceClient;
        this.billServiceClient = billServiceClient;
        this.rabbitTemplate = rabbitTemplate;
    }

    public TransferResponseDTO transfer(TransferRequestDTO transferRequestDTO) {


        Long senderAccountId = transferRequestDTO.getSenderAccountId();
        Long senderBillId = transferRequestDTO.getSenderBillId();
        Long recipientAccountId = transferRequestDTO.getRecipientAccountId();
        Long recipientBillId = transferRequestDTO.getRecipientBillId();
        Boolean isDeposit;

        if (senderAccountId == null && senderBillId == null) {
            throw new TransferServiceException("Sender id is null and recipientId is null");
        }

        if (recipientAccountId == null && recipientBillId == null) {
            throw new TransferServiceException("Recipient id is null and recipientId is null");
        }

        NotificationResponseDTO senderInfo = depositOrPayment(senderAccountId, senderBillId, transferRequestDTO.getAmount(), false);

        NotificationResponseDTO recipientInfo = depositOrPayment(recipientAccountId, recipientBillId, transferRequestDTO.getAmount(), true);
        transferRepository.save(new Transfer(senderInfo.getBillId(), recipientInfo.getBillId(),
                transferRequestDTO.getAmount(), OffsetDateTime.now(), senderInfo.getEmail(), recipientInfo.getEmail()));

        resetActionFlag();

        return new TransferResponseDTO(senderInfo.getBillId(), recipientInfo.getBillId(),
                transferRequestDTO.getAmount(), OffsetDateTime.now(), senderInfo.getEmail(), recipientInfo.getEmail());
    }


    public NotificationResponseDTO depositOrPayment(Long accountId, Long billId, BigDecimal amount, Boolean isDeposit) {

        BigDecimal availableAmount;

        if (billId != null) {

            try {
                BillResponseDTO billResponseDTO = billServiceClient.getBillById(billId);
                BillRequestDTO billRequestDTO = createBillRequest(amount, billResponseDTO, isDeposit);

                AccountResponseDTO accountResponseDTO = accountServiceClient.getAccountById(billResponseDTO.getAccountId());
                billServiceClient.update(billId, billRequestDTO);
                setSuccessFlag(isDeposit);
                availableAmount = billRequestDTO.getAmount();
                return createResponse(billId, amount, accountResponseDTO, availableAmount, isDeposit);

            } catch (FeignException feignException) {
                throw new TransferServiceException("Unable to find bill with id: " + billId);
            }
        }

        BillResponseDTO defaultBill = getDefaultBill(accountId);
        BillRequestDTO billRequestDTO = createBillRequest(amount, defaultBill, isDeposit);
        billServiceClient.update(defaultBill.getBillId(), billRequestDTO);
        setSuccessFlag(isDeposit);
        AccountResponseDTO account = accountServiceClient.getAccountById(accountId);
        availableAmount = billRequestDTO.getAmount();
        return createResponse(defaultBill.getBillId(), amount, account, availableAmount, isDeposit);

    }

    private NotificationResponseDTO createResponse(Long billId, BigDecimal amount, AccountResponseDTO accountResponseDTO, BigDecimal availableAmount,
                                                   Boolean isDeposit) {
        NotificationResponseDTO notificationResponseDTO = new NotificationResponseDTO(billId, amount, accountResponseDTO.getEmail(), availableAmount);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            if (isDeposit) {
                rabbitTemplate.convertAndSend(TOPIC_EXCHANGE_DEPOSIT, ROUTING_KEY_DEPOSIT,
                        objectMapper.writeValueAsString(notificationResponseDTO));
            } else {
                rabbitTemplate.convertAndSend(TOPIC_EXCHANGE_PAYMENT, ROUTING_KEY_PAYMENT,
                        objectMapper.writeValueAsString(notificationResponseDTO));
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new TransferServiceException("Can't send message to RabbitMQ");
        }
        return notificationResponseDTO;
    }

    private BillRequestDTO createBillRequest(BigDecimal amount, BillResponseDTO billResponseDTO, Boolean isDeposit) {
        BillRequestDTO billRequestDTO = new BillRequestDTO();
        billRequestDTO.setAccountId(billResponseDTO.getAccountId());
        billRequestDTO.setCreationDate(billResponseDTO.getCreationDate());
        billRequestDTO.setIsDefault(billResponseDTO.getIsDefault());
        billRequestDTO.setOverdraftEnabled(billResponseDTO.getOverdraftEnabled());
        if (isDeposit) {
            billRequestDTO.setAmount(billResponseDTO.getAmount().add(amount));
        } else {
            if (billResponseDTO.getAmount().subtract(amount).compareTo(BigDecimal.ZERO) != -1 || billResponseDTO.getOverdraftEnabled()) {
                billRequestDTO.setAmount(billResponseDTO.getAmount().subtract(amount));
            } else {
                throw new TransferServiceException("Insufficient funds, available now: " + billResponseDTO.getAmount());
            }
        }
        return billRequestDTO;
    }


    private BillResponseDTO getDefaultBill(Long accountId) {
        return billServiceClient.getBillsByAccountId(accountId).stream()
                .filter(BillResponseDTO::getIsDefault)
                .findAny()
                .orElseThrow(() -> new TransferServiceException("Unable to find default bill for account: " + accountId +
                        ". Please, that accountId is correct and call to you bank manager."));
    }

    public void resetActionFlag() {
        ActionManager.setPaymentSucceed(false);
        ActionManager.setDepositSucceed(false);
    }

    public Transfer getTransferById(Long transferId) {
        return transferRepository.findById(transferId).orElseThrow(() ->
                new TransferServiceException("Unable to find transfer with id: " + transferId));
    }

    public List<Transfer> getTransfersBySenderBillId(Long senderBillId) {
        return transferRepository.getTransfersBySenderBillId(senderBillId);
    }

    public List<Transfer> getTransfersByRecipientBillId(Long recipientBillId) {
        return transferRepository.getTransfersByRecipientBillId(recipientBillId);
    }

    private void setSuccessFlag(Boolean isDeposit) {
        if (isDeposit) {
            ActionManager.setDepositSucceed(true);
        } else {
            ActionManager.setPaymentSucceed(true);
        }
    }
}
