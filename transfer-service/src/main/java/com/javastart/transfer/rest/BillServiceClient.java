package com.javastart.transfer.rest;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@FeignClient(name = "bill-service")
@Validated
public interface BillServiceClient {

    @RequestMapping(value = "bills/{billId}", method = RequestMethod.GET)
    BillResponseDTO getBillById(@PathVariable("billId") @Positive(message = "Please, enter correct Id (Path)")
                                        Long billId);

    @RequestMapping(value = "bills/{billId}", method = RequestMethod.PUT)
    void update(@PathVariable("billId") @Positive(message = "Please, enter correct Id (Path)")
                        Long billId, @Valid BillRequestDTO billRequestDTO);

    @RequestMapping(value = "bills/account/{accountId}", method = RequestMethod.GET)
    List<BillResponseDTO> getBillsByAccountId(@PathVariable("accountId")
                                              @Positive(message = "Please, enter correct Id (Path)") Long accountId);
}
