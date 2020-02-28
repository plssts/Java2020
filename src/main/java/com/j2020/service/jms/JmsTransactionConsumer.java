/**
 * @author Paulius Staisiunas
 */

package com.j2020.service.jms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.j2020.Constants;
import com.j2020.model.BatchOfPayments;
import com.j2020.model.GeneralPayment;
import com.j2020.repository.PaymentBatchRepository;
import com.j2020.repository.TransactionsForBatchRepository;
import com.j2020.service.BankingServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class JmsTransactionConsumer {
    private final Logger logger = LoggerFactory.getLogger(JmsTransactionConsumer.class);
    private BankingServiceFactory serviceFactory;
    private PaymentBatchRepository batchRepository;
    private TransactionsForBatchRepository transactions;

    public JmsTransactionConsumer(BankingServiceFactory serviceFactory,
                                  PaymentBatchRepository batchRepository,
                                  TransactionsForBatchRepository transactions) {
        this.serviceFactory = serviceFactory;
        this.batchRepository = batchRepository;
        this.transactions = transactions;
    }

    @JmsListener(destination = Constants.JMS_TRANSACTION_QUEUE)
    public void onMessage(GeneralPayment payment) {
        List<GeneralPayment> toProcess = new ArrayList<>();
        toProcess.add(payment);
        try {
            logger.info("Consumer received: {}", payment);
            serviceFactory.retrieveTransactionService(payment.getBank()).createPayments(toProcess);

            updateBatchCounters(payment.getBatchId());

        } catch (JsonProcessingException exception) {
            logger.error("Could not process {} because {}", payment, exception.getMessage());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public void updateBatchCounters(Long batchId) {
        Optional<BatchOfPayments> batch = batchRepository.findById(batchId);
        if (batch.isPresent()) {
            BatchOfPayments batchObject = batch.get();
            int size = transactions.findAllByBopid(batchObject.getId()).size();
            batchObject.setCountOfProcessedPayments(size);

            logger.info("Updated processed payments counter");

            batchRepository.save(batchObject);
        }
    }
}