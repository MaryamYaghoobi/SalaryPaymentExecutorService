package ir.dotin.business;

import ir.dotin.files.PaymentVO;

import java.util.List;

import static ir.dotin.PaymentTransactionApp.balanceVOs;
import static ir.dotin.PaymentTransactionApp.transactionVOS;


public class MyThreadPool implements Runnable {
    private String debtorDepositNumber;
    private List<PaymentVO> list;

    public MyThreadPool(String debtorDepositNumber, List<PaymentVO> list) {
        this.debtorDepositNumber = debtorDepositNumber;
        this.list = list;
    }

    @Override
    public void run() {
        for (PaymentVO paymentVO : list) {
            try {
                transactionVOS.add(TransactionProcessor.processPayment(balanceVOs, debtorDepositNumber, paymentVO));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
