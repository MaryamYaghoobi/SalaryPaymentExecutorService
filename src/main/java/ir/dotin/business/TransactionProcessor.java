package ir.dotin.business;

import ir.dotin.PaymentTransactionApp;
import ir.dotin.exception.InadequateInitialBalanceException;
import ir.dotin.exception.NoDepositFoundException;
import ir.dotin.files.BalanceFileHandler;
import ir.dotin.files.BalanceVO;
import ir.dotin.files.PaymentVO;
import ir.dotin.files.TransactionVO;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static ir.dotin.PaymentTransactionApp.balanceVOs;

public class TransactionProcessor {
    public static void processThreadPool(List<PaymentVO> allPaymentVOs) throws InterruptedException, NoDepositFoundException, InadequateInitialBalanceException, IOException, IndexOutOfBoundsException {
        String debtorDepositNumber = getDebtorDepositNumber(allPaymentVOs);
        validationWithdrawals(balanceVOs, allPaymentVOs, debtorDepositNumber);
        List<PaymentVO> paymentVOs = removeDebtorPaymentRecord(allPaymentVOs, debtorDepositNumber);
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        List<List<PaymentVO>> group = new ArrayList<>();
        for (int start = 0; start < paymentVOs.size(); start += 200) {
            group.add(paymentVOs.subList(start, start + 200));
        }
        for (List<PaymentVO> paymentVOList : group) {

            MyThreadPool myThreadPool = new MyThreadPool(debtorDepositNumber, paymentVOList);
            executorService.execute(myThreadPool);

        }
        executorService.shutdown();
        executorService.awaitTermination(5L, TimeUnit.SECONDS);
    }

    private static List<PaymentVO> removeDebtorPaymentRecord(List<PaymentVO> paymentVOs, String debtorDepositNumber) {
        List<PaymentVO> resultPaymentVOs = new ArrayList<>();
        for (PaymentVO paymentVO : paymentVOs) {
            if (!debtorDepositNumber.equals(paymentVO.getDepositNumber())) {
                resultPaymentVOs.add(paymentVO);
            }
        }
        return resultPaymentVOs;
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


    public static TransactionVO processPayment(List<BalanceVO> depositBalances, String debtorDepositNumber, PaymentVO creditorPaymentVO) throws IOException {
        TransactionVO transactionVO = new TransactionVO();
        transactionVO.setDebtorDepositNumber(debtorDepositNumber);
        transactionVO.setCreditorDepositNumber(creditorPaymentVO.getDepositNumber());
        transactionVO.setAmount(creditorPaymentVO.getAmount());
        for (BalanceVO balanceVO : depositBalances) {
            if (balanceVO.getDepositNumber().equals(creditorPaymentVO.getDepositNumber())) {
                balanceVO.setAmount(balanceVO.getAmount().add(creditorPaymentVO.getAmount()));
                updateBalanceFileRecord(creditorPaymentVO.getDepositNumber(), creditorPaymentVO.getAmount());
                transactionVO.setAmount(balanceVO.getAmount());
            }
        }
        updateBalanceFileRecord(debtorDepositNumber, creditorPaymentVO.getAmount().negate());
        return transactionVO;
    }

    public static synchronized void updateBalanceFileRecord(String depositNumber, BigDecimal amountChange) {
        System.out.println("updateBalanceFileRecord, depositNumber = " + depositNumber + ", amountChange = " + amountChange.toString());
        try {
            BufferedReader file = new BufferedReader(new FileReader(PaymentTransactionApp.BALANCE_UPDATE_FILE_PATH));
            StringBuffer inputBuffer = new StringBuffer();
            String line;
            while ((line = file.readLine()) != null) {
                if (line.contains(depositNumber + "\t")) {
                    line = depositNumber + "\t" + new BigDecimal(line.split(depositNumber)[1].trim()).add(amountChange);
                }
                inputBuffer.append(line);
                inputBuffer.append("\r\n");
            }
            file.close();

            FileOutputStream fileOut = new FileOutputStream(PaymentTransactionApp.BALANCE_UPDATE_FILE_PATH,true);
            fileOut.write( inputBuffer.toString().getBytes());
            fileOut.close();
        } catch (Exception e) {
            System.out.println("Problem reading file.");
            e.printStackTrace();
        }
    }
}