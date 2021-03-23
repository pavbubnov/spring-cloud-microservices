package com.javastart.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javastart.notification.config.RabbitMQConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class TransferMessageHandler {

    private final JavaMailSender javaMailSender;

    @Autowired
    public TransferMessageHandler(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_TRANSFER)
    public void receive(Message message) throws JsonProcessingException {

        byte[] body = message.getBody();
        String jsonBody = new String(body);
        ObjectMapper objectMapper = new ObjectMapper();
        TransferResponseDTO transferResponseDTO = objectMapper.readValue(jsonBody, TransferResponseDTO.class);

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(transferResponseDTO.getEmail());
        mailMessage.setFrom("pavbubnovJava@yandex.ru");

        mailMessage.setSubject("Transfer");


        if (!transferResponseDTO.getIsPlus()) {
            mailMessage.setText("Transfer from your bill: " + transferResponseDTO.getSenderBillId() +
                    " to bill: " + transferResponseDTO.getRecipientBillId() +
                    ", sum: " + transferResponseDTO.getAmount() + ", available now: " +
                    transferResponseDTO.getAvailableAmount());
        } else {
            mailMessage.setText("Transfer to your bill : " + transferResponseDTO.getRecipientBillId() +
                    " from bill : " + transferResponseDTO.getSenderBillId() +
                    ", sum :" + transferResponseDTO.getAmount() + ", available now: " +
                    transferResponseDTO.getAvailableAmount());
        }

        javaMailSender.send(mailMessage);

    }
}
