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
import org.springframework.amqp.AmqpConnectException;
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

        checkAllNullException(senderAccountId, senderBillId, recipientAccountId, recipientBillId);

        BillResponseDTO senderBillResponseDTO = createBillResponse(senderBillId, senderAccountId);
        BillRequestDTO senderBillRequestDTO = createBillRequest(amount, senderBillResponseDTO, false);
        AccountResponseDTO senderAccountResponseDTO = createAccountResponse(senderBillResponseDTO.getAccountId());
        senderBillId = senderBillResponseDTO.getBillId();

        BillResponseDTO recipientBillResponseDTO = createBillResponse(recipientBillId, recipientAccountId);
        BillRequestDTO recipientBillRequestDTO = createBillRequest(amount, recipientBillResponseDTO, true);
        AccountResponseDTO recipientAccountResponseDTO = createAccountResponse(recipientBillResponseDTO.getAccountId());
        recipientBillId = recipientBillResponseDTO.getBillId();

        BigDecimal senderAvailableAmount = updateBill(senderBillId, senderBillRequestDTO);
        BigDecimal recipientAvailableAmount = updateBill(recipientBillId, recipientBillRequestDTO);

        Transfer transfer = new Transfer(senderBillId, recipientBillId,
                amount, OffsetDateTime.now(), senderAccountResponseDTO.getEmail(),
                recipientAccountResponseDTO.getEmail());

        transferRepository.save(transfer);
        sendNotification(transfer, senderAvailableAmount, recipientAvailableAmount);

        return new TransferResponseDTO(transfer);
    }

    private void checkAllNullException(Long senderAccountId, Long senderBillId,
                                       Long recipientAccountId, Long recipientBillId) {
        if (senderAccountId == null && senderBillId == null) {
            throw new TransferException("Sender id is null and recipientId is null");
        }

        if (recipientAccountId == null && recipientBillId == null) {
            throw new TransferException("Recipient id is null and recipientId is null");
        }
    }


    private AccountResponseDTO createAccountResponse(Long accountId) {
        try {
            return accountServiceClient.getAccountById(accountId);
        } catch (FeignException feignException) {
            throw new TransferException("Unable to find account with id: " + accountId);
        }
    }

    private BillResponseDTO createBillResponse(Long billId, Long accountId) {

        if (billId != null) {
            try {
                return billServiceClient.getBillById(billId);
            } catch (FeignException feignException) {
                throw new TransferException("Unable to find bill with id: " + billId);
            }
        } else return getDefaultBill(accountId);

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
        } catch (AmqpConnectException e) {
            e.printStackTrace();
            throw new MessageNotSucceedException("Can't send message to RabbitMQ");
        }
    }

    private BigDecimal updateBill(Long billId, BillRequestDTO billRequestDTO) {
        billServiceClient.update(billId, billRequestDTO);
        return billRequestDTO.getAmount();
    }

    private void sendNotification(Transfer transfer, BigDecimal senderAvailableAmount,
                                  BigDecimal recipientAvailableAmount) {

        NotificationResponseDTO senderNotificationResponseDTO = new NotificationResponseDTO(
                transfer.getSenderBillId(), transfer.getRecipientBillId(), transfer.getAmount(),
                transfer.getSenderEmail(), senderAvailableAmount, false);

        NotificationResponseDTO recipientNotificationResponseDTO = new NotificationResponseDTO(
                transfer.getSenderBillId(), transfer.getRecipientBillId(), transfer.getAmount(),
                transfer.getRecipientEmail(), recipientAvailableAmount, true);

        try {
            createResponse(senderNotificationResponseDTO);
        } catch (MessageNotSucceedException exception) {
            throw new RabbitMQException(exception.getMessage() + " , transfer succeed :" +
                    transfer.toString());
        }

        try {
            createResponse(recipientNotificationResponseDTO);
        } catch (MessageNotSucceedException exception) {
            throw new RabbitMQException(exception.getMessage() + " , transfer succeed :" +
                    transfer.toString());
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
