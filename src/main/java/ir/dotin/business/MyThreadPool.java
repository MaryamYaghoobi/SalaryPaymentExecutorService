package ir.dotin.business;

import ir.dotin.PaymentTransactionApp;
import ir.dotin.files.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import static ir.dotin.PaymentTransactionApp.balanceVOs;
import static ir.dotin.PaymentTransactionApp.transactionVOS;

public class MyThreadPool implements Runnable {
    private String name;

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
        try {
            BalanceFileHandler.writeFinalBalanceVOToFile(balanceVOs);
           // TransactionFileHandler.writeTransactionVOToFile(transactionVOS);
        } catch (IOException e) {
            e.printStackTrace();
        }

//-------------------------------------

    }
}




