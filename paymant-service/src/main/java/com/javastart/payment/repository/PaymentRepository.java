package com.javastart.payment.repository;

import com.javastart.payment.entity.Payment;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PaymentRepository extends CrudRepository<Payment, Long> {

    List<Payment> getPaymentByBillId(Long billId);

}
