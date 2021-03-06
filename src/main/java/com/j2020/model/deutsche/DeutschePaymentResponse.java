/**
 * @author Paulius Staisiunas
 */

package com.j2020.model.deutsche;

import com.j2020.model.PaymentResponse;

public class DeutschePaymentResponse implements PaymentResponse {
    private String paymentId;
    private String transactionStatus;

    @Override
    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    @Override
    public String getStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }
}
