/**
 * @author Paulius Staisiunas
 */

package com.j2020.service.revolut;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.j2020.Constants;
import com.j2020.model.*;
import com.j2020.model.revolut.RevolutPayment;
import com.j2020.model.revolut.RevolutPaymentResponse;
import com.j2020.model.revolut.RevolutTransaction;
import com.j2020.repository.TransactionsForBatchRepository;
import com.j2020.service.TransactionRequestRetrievalService;
import com.j2020.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class RevolutTransactionService implements TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(RevolutTransactionService.class);
    private final RevolutTokenService tokenRenewal;
    private final TransactionRequestRetrievalService transactionRetrieval;
    private final RevolutMapperService revolutMapper;
    private TransactionsForBatchRepository transactions;

    @Value("${revolutTransaction.transactionUrl}")
    private String transactionUrl;

    @Value("${revolutTransaction.paymentUrl}")
    private String paymentUrl;

    public RevolutTransactionService(RevolutTokenService tokenRenewal,
                                     TransactionRequestRetrievalService transactionRetrieval,
                                     RevolutMapperService revolutMapper,
                                     TransactionsForBatchRepository transactions) {
        this.tokenRenewal = tokenRenewal;
        this.transactionRetrieval = transactionRetrieval;
        this.revolutMapper = revolutMapper;
        this.transactions = transactions;
    }

    @Override
    public List<GeneralTransaction> retrieveTransactionData(List<String> accountIds) throws JsonProcessingException {
        String oauthToken = tokenRenewal.getToken();
        JavaType type = new ObjectMapper().getTypeFactory().constructCollectionType(List.class, RevolutTransaction.class);

        List<Transaction> response = transactionRetrieval.retrieveTransactions(oauthToken, transactionUrl, type);
        List<GeneralTransaction> parsedTransactions = new ArrayList<>();

        logger.info("Constructing and validating Revolut transactions");
        response.forEach(transaction -> parsedTransactions.add(revolutMapper.toGeneralTransaction((RevolutTransaction) transaction)));

        return parsedTransactions;
    }

    @Override
    public List<PaymentResponse> createPayments(List<GeneralPayment> payments) throws JsonProcessingException {
        if (payments == null) {
            logger.info("No payments included for Revolut. Skipping.");
            return new ArrayList<>();
        }

        List<RevolutPayment> parsedPayments = new ArrayList<>();

        logger.info("Constructing and validating Revolut payments");
        payments.forEach(payment -> parsedPayments.add(revolutMapper.toRevolutPayment(payment)));

        List<PaymentResponse> responses;

        try {
            responses = transactionRetrieval.pushPayments(
                    tokenRenewal.getToken(),
                    paymentUrl,
                    parsedPayments,
                    new ObjectMapper().getTypeFactory().constructType(RevolutPaymentResponse.class));
        } catch (HttpClientErrorException | HttpServerErrorException exception) {
            logger.error("An HTTP error caused payment failure");
            saveFailedStatus(payments.get(0));
            return new ArrayList<>();
        }

        TransactionStatusCheck status = new TransactionStatusCheck();
        status.setPaymentId(responses.get(0).getPaymentId());
        status.setTransactionStatus(responses.get(0).getStatus());
        status.setBatchId(payments.get(0).getBatchId());
        status.setBank(Bank.REVOLUT);
        status.setSourceAccount(payments.get(0).getSourceAccount());
        status.setDestinationAccount(payments.get(0).getDestinationAccount());
        status.setAmount(payments.get(0).getAmount());

        logger.info("Saving the new payment identification and status");
        transactions.save(status);

        return new ArrayList<>();
    }

    private void saveFailedStatus(GeneralPayment payment) {
        logger.warn("Saving an entry for a Revolut payment that failed");

        TransactionStatusCheck status = new TransactionStatusCheck();
        status.setPaymentId(Constants.DISPLAY_FAILED_PAYMENT_ID + Instant.now().toEpochMilli());
        status.setTransactionStatus(Constants.DISPLAY_FAILED_PAYMENT_STATUS);
        status.setBatchId(payment.getBatchId());
        status.setBank(Bank.DEUTSCHE);
        status.setSourceAccount(payment.getSourceAccount());
        status.setDestinationAccount(payment.getDestinationAccount());
        status.setAmount(payment.getAmount());

        transactions.save(status);
    }

    @Override
    public boolean canProcessThisBank(Bank bankingService) {
        return bankingService.equals(Bank.REVOLUT);
    }
}
