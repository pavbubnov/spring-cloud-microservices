package com.javastart.transfer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javastart.transfer.controller.dto.NotificationResponseDTO;
import com.javastart.transfer.controller.dto.TransferRequestDTO;
import com.javastart.transfer.controller.dto.TransferResponseDTO;
import com.javastart.transfer.entity.Transfer;
import com.javastart.transfer.exception.TransferException;
import com.javastart.transfer.exception.RabbitMQException;
import com.javastart.transfer.exception.MessageNotSucceedException;
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

    private static final String TOPIC_EXCHANGE_TRANSFER = "js.transfer.notify.exchange";
    private static final String ROUTING_KEY_TRANSFER = "js.key.transfer";

    private final TransferRepository transferRepository;

    private final AccountServiceClient accountServiceClient;

    private final BillServiceClient billServiceClient;

    private final RabbitTemplate rabbitTemplate;


    @Autowired
    public TransferService(TransferRepository transferRepository, AccountServiceClient accountServiceClient,
                           BillServiceClient billServiceClient, RabbitTemplate rabbitTemplate) {
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
        BigDecimal amount = transferRequestDTO.getAmount();

        BigDecimal senderAvailableAmount;
        BigDecimal recipientAvailableAmount;

        BillRequestDTO senderBillRequestDTO;
        BillResponseDTO senderBillResponseDTO;
        AccountResponseDTO senderAccountResponseDTO;
        BillRequestDTO recipientBillRequestDTO;
        BillResponseDTO recipientBillResponseDTO;
        AccountResponseDTO recipientAccountResponseDTO;


        if (senderAccountId == null && senderBillId == null) {
            throw new TransferException("Sender id is null and recipientId is null");
        }

        if (recipientAccountId == null && recipientBillId == null) {
            throw new TransferException("Recipient id is null and recipientId is null");
        }

        if (senderBillId != null) {
            senderBillResponseDTO = createBillResponse(senderBillId);
            senderBillRequestDTO = createBillRequest(amount, senderBillResponseDTO, false);
            senderAccountResponseDTO = createAccountResponse(senderBillResponseDTO.getAccountId());
        } else {
            senderBillResponseDTO = getDefaultBill(senderAccountId);
            senderBillRequestDTO = createBillRequest(amount, senderBillResponseDTO, false);
            senderAccountResponseDTO = accountServiceClient.getAccountById(senderAccountId);
            senderBillId = senderBillResponseDTO.getBillId();
        }

        if (recipientBillId != null) {
            recipientBillResponseDTO = createBillResponse(recipientBillId);
            recipientBillRequestDTO = createBillRequest(amount, recipientBillResponseDTO, true);
            recipientAccountResponseDTO = createAccountResponse(recipientBillResponseDTO.getAccountId());
        } else {
            recipientBillResponseDTO = getDefaultBill(recipientAccountId);
            recipientBillRequestDTO = createBillRequest(amount, recipientBillResponseDTO, true);
            recipientAccountResponseDTO = accountServiceClient.getAccountById(recipientAccountId);
            recipientBillId = recipientBillResponseDTO.getBillId();
        }


        billServiceClient.update(senderBillId, senderBillRequestDTO);
        senderAvailableAmount = senderBillRequestDTO.getAmount();


        billServiceClient.update(recipientBillId, recipientBillRequestDTO);
        recipientAvailableAmount = recipientBillRequestDTO.getAmount();

        NotificationResponseDTO senderNotificationResponseDTO = new NotificationResponseDTO(
                senderBillId, recipientBillId, amount, senderAccountResponseDTO.getEmail(),
                senderAvailableAmount, false);

        NotificationResponseDTO recipientNotificationResponseDTO = new NotificationResponseDTO(
                senderBillId, recipientBillId, amount, recipientAccountResponseDTO.getEmail(),
                recipientAvailableAmount, true);

        transferRepository.save(new Transfer(senderBillId, recipientBillId,
                transferRequestDTO.getAmount(), OffsetDateTime.now(),
                senderAccountResponseDTO.getEmail(), recipientAccountResponseDTO.getEmail()));

        try {
            createResponse(senderNotificationResponseDTO);
        } catch (MessageNotSucceedException exception) {
            throw new RabbitMQException(exception.getMessage());
        }

        try {
            createResponse(recipientNotificationResponseDTO);
        } catch (MessageNotSucceedException exception) {
            throw new RabbitMQException(exception.getMessage());
        }

        return new TransferResponseDTO(senderBillId, recipientBillId,
                amount, OffsetDateTime.now(), senderAccountResponseDTO.getEmail(),
                recipientAccountResponseDTO.getEmail());
    }


    private AccountResponseDTO createAccountResponse(Long accountId) {
        try {
            return accountServiceClient.getAccountById(accountId);
        } catch (FeignException feignException) {
            throw new TransferException("Unable to find account with id: " + accountId);
        }
    }

    private BillResponseDTO createBillResponse(Long billId) {
        try {
            return billServiceClient.getBillById(billId);
        } catch (FeignException feignException) {
            throw new TransferException("Unable to find bill with id: " + billId);
        }
    }


    private BillRequestDTO createBillRequest(BigDecimal amount, BillResponseDTO billResponseDTO, Boolean isPlus) {
        BillRequestDTO billRequestDTO = new BillRequestDTO();
        billRequestDTO.setAccountId(billResponseDTO.getAccountId());
        billRequestDTO.setCreationDate(billResponseDTO.getCreationDate());
        billRequestDTO.setIsDefault(billResponseDTO.getIsDefault());
        billRequestDTO.setOverdraftEnabled(billResponseDTO.getOverdraftEnabled());
        if (isPlus) {
            billRequestDTO.setAmount(billResponseDTO.getAmount().add(amount));
        } else {
            if (billResponseDTO.getAmount().subtract(amount).compareTo(BigDecimal.ZERO) != -1 ||
                    billResponseDTO.getOverdraftEnabled()) {
                billRequestDTO.setAmount(billResponseDTO.getAmount().subtract(amount));
            } else {
                throw new TransferException("Insufficient funds, available now: " +
                        billResponseDTO.getAmount());
            }
        }
        return billRequestDTO;
    }

    private BillResponseDTO getDefaultBill(Long accountId) {
        return billServiceClient.getBillsByAccountId(accountId).stream()
                .filter(BillResponseDTO::getIsDefault)
                .findAny()
                .orElseThrow(() -> new TransferException("Unable to find default bill for account: " +
                        accountId + ". Please, check that accountId is correct."));
    }


    private void createResponse(NotificationResponseDTO notificationResponseDTO) throws MessageNotSucceedException {

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            rabbitTemplate.convertAndSend(TOPIC_EXCHANGE_TRANSFER, ROUTING_KEY_TRANSFER,
                    objectMapper.writeValueAsString(notificationResponseDTO));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new MessageNotSucceedException("Can't send message to RabbitMQ");
        }
    }


    public Transfer getTransferById(Long transferId) {
        return transferRepository.findById(transferId).orElseThrow(() ->
                new TransferException("Unable to find transfer with id: " + transferId));
    }

    public List<Transfer> getTransfersBySenderBillId(Long senderBillId) {
        return transferRepository.getTransfersBySenderBillId(senderBillId);
    }

    public List<Transfer> getTransfersByRecipientBillId(Long recipientBillId) {
        return transferRepository.getTransfersByRecipientBillId(recipientBillId);
    }

}
