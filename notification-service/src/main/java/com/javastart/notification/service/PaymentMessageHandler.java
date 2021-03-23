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
public class PaymentMessageHandler {

    private final JavaMailSender javaMailSender;

    @Autowired
    public PaymentMessageHandler(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PAYMENT)
    public void receive(Message message) throws JsonProcessingException {

        byte[] body = message.getBody();
        String jsonBody = new String(body);
        ObjectMapper objectMapper = new ObjectMapper();
        PaymentResponseDTO paymentResponseDTO = objectMapper.readValue(jsonBody, PaymentResponseDTO.class);


        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(paymentResponseDTO.getEmail());
        mailMessage.setFrom("pavbubnovJava@yandex.ru");

        mailMessage.setSubject("Payment");
        mailMessage.setText("Payment from your bill " + paymentResponseDTO.getBillId() +
                ", sum:" + paymentResponseDTO.getAmount() + ", available now: " +
                paymentResponseDTO.getAvailableAmount());

        javaMailSender.send(mailMessage);

    }
}
