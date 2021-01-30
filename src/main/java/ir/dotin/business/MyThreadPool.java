package ir.dotin.business;

import ir.dotin.files.BalanceFileHandler;
import ir.dotin.files.PaymentVO;

import java.io.IOException;
import java.util.List;

import static ir.dotin.PaymentTransactionApp.balanceVOs;

public class MyThreadPool implements Runnable {
    private String name;

    public List getList() {
        return list;
    }

    public void setList(List list) {
        this.list = list;
    }

    private List list;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MyThreadPool(List<PaymentVO> list) {
        this.list = list;
    }

    @Override
    public void run() {
        try {
            BalanceFileHandler.writeFinalBalanceVOToFile(balanceVOs);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}




