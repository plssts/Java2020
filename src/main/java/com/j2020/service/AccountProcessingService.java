/**
 * @author Paulius Staisiunas
 */

package com.j2020.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.j2020.model.Account;
import com.j2020.model.Bank;
import com.j2020.model.GeneralAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccountProcessingService {
    private final BankingServiceFactory bankingService;
    private static final Logger logger = LoggerFactory.getLogger(AccountProcessingService.class);

    public AccountProcessingService(BankingServiceFactory bankingService) {
        this.bankingService = bankingService;
    }

    public Map<String, List<GeneralAccount>> collectAccountResponse() throws JsonProcessingException {
        logger.info("Retrieving account entries");
        List<GeneralAccount> accountsRevo = bankingService.retrieveAccountService(Bank.REVOLUT).retrieveAccountData();
        List<GeneralAccount> accountsDeut = bankingService.retrieveAccountService(Bank.DEUTSCHE).retrieveAccountData();

        Map<String, List<GeneralAccount>> outcome = new HashMap<>();
        outcome.put(Bank.REVOLUT.toString(), accountsRevo);
        outcome.put(Bank.DEUTSCHE.toString(), accountsDeut);

        return outcome;
    }
}
