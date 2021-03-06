/**
 * @author Paulius Staisiunas
 */

package com.j2020.model.revolut;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RevolutTransactionLegCounterparty {
    private Long id;

    @JsonProperty(value = "counterparty_id")
    @JsonAlias("id")
    private String counterpartyId;

    @JsonProperty(value = "account_type")
    private String accountType;

    @JsonProperty(value = "account_id")
    private String accountId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getCounterpartyId() {
        return counterpartyId;
    }

    public void setCounterpartyId(String counterpartyId) {
        this.counterpartyId = counterpartyId;
    }

    @Override
    public String toString() {
        return "RevolutTransactionLegCounterparty{" +
                "counterpartyId='" + counterpartyId + '\'' +
                ", accountType='" + accountType + '\'' +
                ", accountId='" + accountId + '\'' +
                '}';
    }
}
