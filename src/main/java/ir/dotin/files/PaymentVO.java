package ir.dotin.files;

import ir.dotin.business.DepositType;

import java.io.Serializable;
import java.math.BigDecimal;

public class PaymentVO implements Serializable {
    DepositType type;
    String depositNumber;
    BigDecimal amount;

    public PaymentVO(DepositType type, String depositNumber, BigDecimal amount) {
        this.type = type;
        this.depositNumber = depositNumber;
        this.amount = amount;
    }

    public PaymentVO() {

    }

    public DepositType getType() {
        return type;
    }

    public void setType(DepositType type) {
        this.type = type;
    }

    public String getDepositNumber() {
        return depositNumber;
    }

    public void setDepositNumber(String depositNumber) {
        this.depositNumber = depositNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }


    @Override
    public String toString() {
        return type + "\t" + depositNumber + "\t" + amount;
    }

}
