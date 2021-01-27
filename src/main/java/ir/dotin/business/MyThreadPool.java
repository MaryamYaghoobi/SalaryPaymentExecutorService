package ir.dotin.business;

import ir.dotin.PaymentTransactionApp;
import ir.dotin.files.BalanceVO;
import ir.dotin.files.PaymentVO;
import ir.dotin.files.TransactionVO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import static ir.dotin.PaymentTransactionApp.balanceVOs;


public class MyThreadPool implements Runnable {
    private String name;
    private TransactionVO[] transactionVOs;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MyThreadPool(PaymentVO name) {
        this.name = String.valueOf(name);
    }

    @Override
    public void run() {
//-------------------------------------

        File fileBalance = new File(PaymentTransactionApp.BALANCE_UPDATE_FILE_PATH);
        File fileTransaction = new File(PaymentTransactionApp.TRANSACTION_FILE_PATH);
        // file.writeFinalBalanceVOToFileThreadPool(balanceVOs);
        System.out.println(this.getName());

//-------------------------------------
        // public static synchronized void writeFinalBalanceVOToFileThreadPool(List < BalanceVO > balanceVOs){
        synchronized (this) {
            PrintWriter printWriter = null;
            try {
                printWriter = new PrintWriter(fileBalance);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            for (BalanceVO balanceVO : balanceVOs) {
                printWriter.println(balanceVO.toString());
            }
            printWriter.close();
        }
        synchronized (this) {
            PrintWriter printWriter = null;
            try {
                printWriter = new PrintWriter(fileTransaction);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            for (TransactionVO transactionVO : transactionVOs) {
                printWriter.println(transactionVO.toString());
            }
            printWriter.close();
        }
    }
}




