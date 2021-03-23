package com.javastart.transfer.repository;

import com.javastart.transfer.entity.Transfer;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TransferRepository extends CrudRepository<Transfer, Long> {

    List<Transfer> getTransfersBySenderBillId(Long senderBillId);

    List<Transfer> getTransfersByRecipientBillId(Long recipientBillId);
}
