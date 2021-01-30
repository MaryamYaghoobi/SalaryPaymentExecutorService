package ir.dotin.business;

import ir.dotin.exception.InadequateInitialBalanceException;
import ir.dotin.exception.NoDepositFoundException;
import ir.dotin.files.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TransactionProcessor {

    public static List<TransactionVO> processThreadPool(List<BalanceVO> depositBalances, List<PaymentVO> paymentVOs) throws InterruptedException, NoDepositFoundException, InadequateInitialBalanceException, IOException,IndexOutOfBoundsException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        List<TransactionVO> transactionVOs = new ArrayList<>();
        BalanceFileHandler.createFinalBalanceFile(depositBalances);
        TransactionFileHandler.createTransactionFile(transactionVOs, depositBalances);
       // List<PaymentVO> list = new ArrayList<>();
//=================================
        for (int start = 0; start < paymentVOs.size(); start += 200) {
            List<PaymentVO> group = paymentVOs.subList(start, start + 200);
//=================================
        /*
        List<PaymentVO> paymentVOs1 = list.subList(0, 201);
        List<PaymentVO> paymentVOs2 = list.subList(201, 401);
        List<PaymentVO> paymentVOs3 = list.subList(401, 601);
        List<PaymentVO> paymentVOs4 = list.subList(601, 801);
        List<PaymentVO> paymentVOs5 = list.subList(801, 1002);
        list.add((PaymentVO) paymentVOs1);
        list.add((PaymentVO) paymentVOs2);
        list.add((PaymentVO) paymentVOs3);
        list.add((PaymentVO) paymentVOs4);
        list.add((PaymentVO) paymentVOs5);*/
           // BalanceFileHandler.createFinalBalanceFile(depositBalances);
           // TransactionFileHandler.createTransactionFile(transactionVOs, depositBalances);
            String debtorDepositNumber = getDebtorDepositNumber(group);
            validationWithdrawals(depositBalances, group, debtorDepositNumber);
           for (PaymentVO paymentVO : group) {
               if (DepositType.CREDITOR.equals(paymentVO.getType())) {
                   MyThreadPool myThreadPool = new MyThreadPool(paymentVO);
                   executorService.execute(myThreadPool);
                   // MyThreadPool myThreadPool = new MyThreadPool(list);
                   transactionVOs.add(processPayment(depositBalances, debtorDepositNumber, paymentVO));

               }

           }
                /*
                MyThreadPool myThreadPool1 = new MyThreadPool(paymentVOs1);
                MyThreadPool myThreadPool2 = new MyThreadPool(paymentVOs2);
                MyThreadPool myThreadPool3 = new MyThreadPool(paymentVOs3);
                MyThreadPool myThreadPool4 = new MyThreadPool(paymentVOs4);
                MyThreadPool myThreadPool5 = new MyThreadPool(paymentVOs5);*/
               /* executorService.execute(myThreadPool1);
                executorService.execute(myThreadPool2);
                executorService.execute(myThreadPool3);
                executorService.execute(myThreadPool4);
                executorService.execute(myThreadPool5);*/
                // executorService.execute(new MyThreadPool(Collections.singletonList(paymentVO)));
                //  executorService.execute(myThreadPool);
                // transactionVOs.add(processPayment(depositBalances, debtorDepositNumber, paymentVO));

           // }
            executorService.shutdown();

            executorService.awaitTermination(200L, TimeUnit.MICROSECONDS);

          //  return transactionVOs;
        }
        return transactionVOs;
    }

    public static String getDebtorDepositNumber(List<PaymentVO> paymentVOs) throws NoDepositFoundException {
        for (PaymentVO paymentVO : paymentVOs) {
            if (DepositType.DEBTOR.equals(paymentVO.getType())) {
                return paymentVO.getDepositNumber();
            }
        }
        throw new NoDepositFoundException("Debtor deposit not found!");
    }

    private static void validationWithdrawals(List<BalanceVO> depositBalances, List<PaymentVO> paymentVOs, String debtorDepositNumber) throws NoDepositFoundException, InadequateInitialBalanceException {
        BigDecimal totalCreditorAmount = getCreditorAmountsSum(paymentVOs);
        String depositNumber = "";
        for (BalanceVO balanceVO : depositBalances) {
            if (balanceVO.getDepositNumber().equals(depositNumber))
                balanceVO.getAmount();
            BigDecimal debtorBalance = balanceVO.getAmount();
            if (debtorBalance == null)
                throw new NoDepositFoundException("Debtor balance not found!");
            if (totalCreditorAmount.compareTo(debtorBalance) == 1)
                throw new InadequateInitialBalanceException("Not enough balance!");
        }
    }

    private static BigDecimal getCreditorAmountsSum(List<PaymentVO> paymentVOs) {
        BigDecimal totalCreditorAmount = new BigDecimal(0);
        for (PaymentVO paymentVO : paymentVOs) {
            if (DepositType.CREDITOR.equals(paymentVO.getType())) {
                totalCreditorAmount.add(paymentVO.getAmount());
            }
        }
        return totalCreditorAmount;
    }

    private static TransactionVO processPayment(List<BalanceVO> depositBalances, String debtorDepositNumber, PaymentVO creditorPaymentVO) {

        TransactionVO transactionVO = new TransactionVO();
        transactionVO.setDebtorDepositNumber(debtorDepositNumber);
        transactionVO.setCreditorDepositNumber(creditorPaymentVO.getDepositNumber());
        transactionVO.setAmount(creditorPaymentVO.getAmount());
        for (BalanceVO balanceVO : depositBalances) {
            if (balanceVO.getDepositNumber().equals(creditorPaymentVO.getDepositNumber())) {//Creditor
                balanceVO.setAmount(balanceVO.getAmount().add(creditorPaymentVO.getAmount()));
                transactionVO.setAmount(balanceVO.getAmount());
            } else if (balanceVO.getDepositNumber().equals(debtorDepositNumber)) {//Debtor
                balanceVO.setAmount(balanceVO.getAmount().subtract(creditorPaymentVO.getAmount()));
                transactionVO.setAmount(balanceVO.getAmount());
            }
        }

        return transactionVO;
    }
}








