package com.javastart.transfer.rest;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.constraints.Positive;

@FeignClient(name = "account-service")
@Transactional
public interface AccountServiceClient {

    @RequestMapping(value = "/accounts/{accountId}", method = RequestMethod.GET)
    AccountResponseDTO getAccountById(@PathVariable("accountId") @Positive(message = "Please, enter correct Id (Path)")
                                              Long accountId);
}
