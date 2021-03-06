package com.javastart.bill.service;

import com.javastart.bill.entity.Bill;
import com.javastart.bill.exception.BillCreateException;
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

/**
 * @author Pavel Bubnov
 */
@Service
public class BillService {

    private final BillRepository billRepository;

    private final AccountServiceClient accountServiceClient;

    @Autowired
    public BillService(BillRepository billRepository, AccountServiceClient accountServiceClient) {
        this.billRepository = billRepository;
        this.accountServiceClient = accountServiceClient;
    }

    /**
     * Get bill information by bill id
     *
     * @param billId - Id number of requested bill
     * @return {@link Bill} - full database information about requested bill
     * @throws BillCreateException - if unable to find bill with necessary id
     */
    public Bill getBillById(Long billId) {
        return billRepository.findById(billId)
                .orElseThrow(() -> new BillCreateException("Unable to find bill with id: " + billId));
    }

    /**
     * Create bill owned by account
     * <p>
     * Not supported null values of params
     * <p>
     * Bill id must be contained by account{@link AccountServiceClient#getAccountById},
     * <p>
     * Account must contain default bill anyway and default bill must be the only one:
     * a)If it is firth bill of account - it automatically created as default bill, not depend of isDefault field
     * b)If default bill has already existed and it is created new default bill - previous default bill
     * automatically updated as not default {@link BillService#reDefaultBill}
     * <p>
     * It is not allowed to create bill that has already created {@link BillService#getBillsByAccountId},
     * {@link BillService#getExistenceBillIdList} - it's allowed to update it (use {@link BillService#updateBill})
     *
     * @param accountId        - Id number of account contained created bill
     * @param billId           - Id number of created bill
     * @param amount           - Start amount of the bill
     * @param isDefault        - Is it default bill or not
     * @param overdraftEnabled - Is it enable to negative balance
     * @return {@link Long} created bill id
     * @throws BillCreateException in several cases:
     *                             a) Bill id doesn't contained by account
     *                             b) Bill has already created
     *                             c) There is no account with request account id
     */
    public Long createBill(Long accountId, Long billId, BigDecimal amount, Boolean isDefault, Boolean overdraftEnabled) {

        try {
            AccountResponseDTO accountResponseDTO = accountServiceClient.getAccountById(accountId);

            if (!accountResponseDTO.getBills().contains(billId)) {
                throw new BillCreateException("Account with id: " + accountId + " doesn't contain bill with id: "
                        + billId);
            }
        } catch (FeignException exception) {
            throw new BillCreateException("Unable to find account with id: " + accountId);
        }

        List<Bill> billsList = getBillsByAccountId(accountId);

        if (getExistenceBillIdList(accountId).contains(billId)) {
            throw new BillCreateException("Bill with id: " + billId + " is already exists");
        }

        if (billsList.size() == 0) {
            isDefault = true;
        } else if (isDefault) {
            reDefaultBill(billsList);
        }

        Bill bill = new Bill(accountId, billId, amount, isDefault, OffsetDateTime.now(), overdraftEnabled);
        return billRepository.save(bill).getBillId();

    }

    /**
     * Update bill has already created
     * <p>
     * Bill id must be contained by account{@link BillService#getBillsByAccountId},
     * {@link BillService#getExistenceBillIdList}
     * <p>
     * Not supported null values of params, it is required to repeat values if no updating
     * <p>
     * Account must contain default bill anyway and default bill must be the only one:
     * a)If default bill has already existed and it is updating another bill as new default bill - previous default bill
     * automatically updated as not default {@link BillService#reDefaultBill}
     * b)It is not allowed to update default bill as not default - firstly update new default bill
     *
     * @param billId           - Id number of updating bill
     * @param accountId        - Id number of account contained updating bill
     * @param amount           - Updating amount of the bill
     * @param isDefault        - Is it default bill or not (updating)
     * @param overdraftEnabled - Is it enable to negative balance (updating)
     * @return {@link Bill} full database information about updated bill
     * @throws BillCreateException - a) if bill hasn't belonged to account or hasn,t create yes
     *                             b) updating default bill as not default
     */
    public Bill updateBill(Long billId, Long accountId, BigDecimal amount,
                           Boolean isDefault, Boolean overdraftEnabled) {

        if (billId == null) {
            throw new BillCreateException("Enter bill id");
        }

        List<Bill> billsList = getBillsByAccountId(accountId);

        if (!getExistenceBillIdList(accountId).contains(billId)) {
            throw new BillCreateException("Bill with id: " + billId + " is not belongs to account with id: "
                    + accountId + " or has't created yet");
        }

        Bill oldDefaultBill = billsList.stream().filter(Bill::getIsDefault).findAny().get();

        if (isDefault) {
            if (oldDefaultBill.getBillId() != billId) {
                reDefaultBill(billsList);
            }
        } else {
            if ((oldDefaultBill.getBillId() == billId) && !RedefaultManager.enableReDefault) {
                throw new BillCreateException("Your account must contain default bill");
            } else RedefaultManager.enableReDefault = false;
        }

        Bill bill = new Bill(accountId, billId, amount, isDefault,
                getBillById(billId).getCreationDate(), overdraftEnabled);
        return billRepository.save(bill);
    }

    /**
     * Delete bill from database
     * <p>
     * Exception from {@link BillService#getBillById} if unable to find requested bill
     *
     * @param billId - Id number of deleting bill
     * @return {@link Bill} - last database information before deleting
     */
    public Bill deleteBill(Long billId) {
        Bill deleteBill = getBillById(billId);
        billRepository.deleteById(billId);
        return deleteBill;
    }

    /**
     * Get full information about all bills contains account
     *
     * @param accountId - id number of requested account
     * @return {@link List<Bill>} - full database information about bills contains requested account
     * No exceptions if unable to find requested account or account doesn't contain bills - @return []
     */
    public List<Bill> getBillsByAccountId(Long accountId) {
        return billRepository.getBillsByAccountIdOrderByBillId(accountId);
    }

    /**
     * Find default bill in list and send command to make it not default
     * {@link BillService#updateBill}
     *
     * @param billList - list of full database information about bills contains requested account
     */
    private void reDefaultBill(List<Bill> billList) {
        Bill oldDefaultBill = billList.stream().filter(Bill::getIsDefault).findAny().get();
        RedefaultManager.enableReDefault = true;
        updateBill(oldDefaultBill.getBillId(), oldDefaultBill.getAccountId(), oldDefaultBill.getAmount(),
                false, oldDefaultBill.getOverdraftEnabled());
    }

    /**
     * Get list of bills id contains account
     *
     * @param accountId - id number of requested account
     * @return {@link List<Long>} - list of id numbers contains requested account
     * No exceptions if unable to find requested account or account doesn't contain bills - @return []
     */
    private List<Long> getExistenceBillIdList(Long accountId) {
        return getBillsByAccountId(accountId).stream().map(bill -> bill.getBillId()).collect(Collectors.toList());
    }

}
