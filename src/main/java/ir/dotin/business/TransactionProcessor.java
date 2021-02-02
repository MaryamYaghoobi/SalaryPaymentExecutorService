package ir.dotin.business;

import ir.dotin.PaymentTransactionApp;
import ir.dotin.exception.InadequateInitialBalanceException;
import ir.dotin.exception.NoDepositFoundException;
import ir.dotin.files.BalanceFileHandler;
import ir.dotin.files.BalanceVO;
import ir.dotin.files.PaymentVO;
import ir.dotin.files.TransactionVO;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
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
                finalBalanceFile(Collections.singletonList(balanceVO));
                transactionVO.setAmount(balanceVO.getAmount());
            } else if (balanceVO.getDepositNumber().equals(debtorDepositNumber)) {
                // balanceVO.setDepositNumber(balanceVO.getDepositNumber());
                balanceVO.setAmount(balanceVO.getAmount().subtract(creditorPaymentVO.getAmount()));
                finalDebtorBalanceFile(depositBalances);
                // transactionVO.setAmount(balanceVO.getAmount());
                //  finalBalanceFileDebtor(depositBalances, debtorDepositNumber, creditorPaymentVO);
            }
        }

        return transactionVO;
    }

    public static void finalBalanceFile(List<BalanceVO> depositBalances) throws IOException {
        BalanceFileHandler.createFinalBalanceFile(depositBalances);
        BalanceFileHandler.writeFinalBalanceVOToFile(depositBalances);
        BalanceFileHandler.printFinalBalanceVOsToConsole(depositBalances);
    }

    public static void finalDebtorBalanceFile(List<BalanceVO> depositBalances) throws IOException {
        BalanceFileHandler.createFinalBalanceFile(depositBalances);
        writeFinalDebtorBalanceVOToFile(depositBalances);
        BalanceFileHandler.printFinalBalanceVOsToConsole(depositBalances);
    }

    public static synchronized void writeFinalDebtorBalanceVOToFile(List<BalanceVO> balanceVOs) throws IOException {
        FileWriter fileWriterBalance = new FileWriter(PaymentTransactionApp.BALANCE_UPDATE_FILE_PATH, true);
        PrintWriter printWriter = new PrintWriter(fileWriterBalance);
        String debtor = PaymentTransactionApp.DEBTOR_DEPOSIT_NUMBER;
        //  int count = 0;
        Scanner scanner = new Scanner(PaymentTransactionApp.FILE_PATH_PREFIX + "BalanceUpdate.txt");
        while (scanner.hasNextLine()) {
            String nextToken = scanner.next();
            // if (nextToken.equalsIgnoreCase(debtor))
            if (nextToken.contains(debtor))
                debtor = nextToken;

            //  count++;
        }

        String d = null;
        for (BalanceVO balanceVO : balanceVOs) {
            if (balanceVO.getDepositNumber().equals(debtor)) {
                // printWriter.println(balanceVO.setDepositNumber(debtor) + " " + balanceVO.getAmount());
                d = balanceVO.setDepositNumber(debtor);
            }
        }
        printWriter.println(d);

        printWriter.close();
    }
}
