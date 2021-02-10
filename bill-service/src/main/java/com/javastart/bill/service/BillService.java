package com.javastart.bill.service;

import com.javastart.bill.entity.Bill;
import com.javastart.bill.exception.BillNotFoundException;
import com.javastart.bill.repository.BillRepository;
import com.javastart.bill.rest.AccountResponseDTO;
import com.javastart.bill.rest.AccountServiceClient;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BillService {

    private final BillRepository billRepository;

    private final AccountServiceClient accountServiceClient;

    @Autowired
    public BillService(BillRepository billRepository, AccountServiceClient accountServiceClient) {
        this.billRepository = billRepository;
        this.accountServiceClient = accountServiceClient;
    }

    public Bill getBillById(Long billId) {
        return billRepository.findById(billId)
                .orElseThrow(() -> new BillNotFoundException("Unable to find bill with id: " + billId));
    }

    public Long createBill(Long accountId, Long billId, BigDecimal amount, Boolean isDefault, Boolean overdraftEnabled)
            throws FeignException {

        AccountResponseDTO accountResponseDTO = accountServiceClient.getAccountById(accountId);

        if (!accountResponseDTO.getBills().contains(billId)) {
            throw new BillNotFoundException("Account with id: " + accountId + " doesn't contain bill with id: " + billId);
        }

        List<Bill> billsList = getBillsByAccountId(accountId);

        if (getExistenceBillIdList(accountId).contains(billId)) {
            throw new BillNotFoundException("Bill with id: " + billId + " is already exists");
        }

        if (billsList.size() == 0) {
            isDefault = true;
        } else if (isDefault) {
            reDefaultBill(billsList);
        }

        Bill bill = new Bill(accountId, billId, amount, isDefault, OffsetDateTime.now(), overdraftEnabled);
        return billRepository.save(bill).getBillId();

    }

    public Bill updateBill(Long billId, Long accountId, BigDecimal amount,
                           Boolean isDefault, Boolean overdraftEnabled) {

        if (billId == null) {
            throw new BillNotFoundException("Enter bill id");
        }

        List<Bill> billsList = getBillsByAccountId(accountId);

        if (!getExistenceBillIdList(accountId).contains(billId)) {
            throw new BillNotFoundException("Bill with id: " + billId + " is not belongs to account with id: " + accountId +
                    " or has't created yet");
        }

        if (isDefault) {
            Bill oldDefaultBill = billsList.stream().filter(Bill::getIsDefault).findAny().get();
            if (oldDefaultBill.getBillId() != billId) {
                reDefaultBill(billsList);
            }
        }

        Bill bill = new Bill(accountId, billId, amount, isDefault,
                getBillById(billId).getCreationDate(), overdraftEnabled);
        return billRepository.save(bill);
    }

    public String deleteBill(Long billId) {
        Bill deleteBill = getBillById(billId);
        billRepository.deleteById(billId);
        return "Bill with id: " + billId + " was deleted";
    }

    public List<Bill> getBillsByAccountId(Long accountId) {
        return billRepository.getBillsByAccountId(accountId);
    }

    private void reDefaultBill(List<Bill> billList) {
        Bill oldDefaultBill = billList.stream().filter(Bill::getIsDefault).findAny().get();
        updateBill(oldDefaultBill.getBillId(), oldDefaultBill.getAccountId(), oldDefaultBill.getAmount(),
                false, oldDefaultBill.getOverdraftEnabled());
    }

    private List<Long> getExistenceBillIdList(Long accountId) {
        return getBillsByAccountId(accountId).stream().map(bill -> bill.getBillId()).collect(Collectors.toList());
    }

}
