package com.j2020.model.deutsche;

public class DeutscheSepaPaymentRequestData {
    private String type = "challengeRequestDataInstantSepaCreditTransfers";
    private String targetIban;
    private String amountCurrency;
    private String amountValue;

    public DeutscheSepaPaymentRequestData() {

    }

    public DeutscheSepaPaymentRequestData(String targetIban, String amountCurrency, String amountValue) {
        this.targetIban = targetIban;
        this.amountCurrency = amountCurrency;
        this.amountValue = formatValue(amountValue);
    }

    private String formatValue(String value) {
        StringBuilder builder = new StringBuilder(value);

        if (builder.indexOf(".") > 0){
            while (builder.substring(builder.length() - 1).equals("0")){
                builder = builder.deleteCharAt(builder.length() - 1);
            }
            if (builder.substring(builder.length() - 1).equals(".")){
                builder = builder.deleteCharAt(builder.length() - 1);
            }
        }
        return builder.toString();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTargetIban() {
        return targetIban;
    }

    public void setTargetIban(String targetIban) {
        this.targetIban = targetIban;
    }

    public String getAmountCurrency() {
        return amountCurrency;
    }

    public void setAmountCurrency(String amountCurrency) {
        this.amountCurrency = amountCurrency;
    }

    public String getAmountValue() {
        return amountValue;
    }

    public void setAmountValue(String amountValue) {
        this.amountValue = amountValue;
    }
}