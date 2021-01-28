package ir.dotin.files;

import ir.dotin.PaymentTransactionApp;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


public class TransactionFileHandler {
    public static String createTransactionFile(List<TransactionVO> transactionVOS, List<BalanceVO> depositBalances) throws IOException {
        String resultTransaction = "";
        Path pathTransaction = Paths.get(PaymentTransactionApp.FILE_PATH_PREFIX + "Transactions.txt");
        Files.createFile(pathTransaction);
        writeTransactionVOToFile(transactionVOS);
        resultTransaction = resultTransaction + (PaymentTransactionApp.DEBTOR_DEPOSIT_NUMBER + " " + PaymentTransactionApp.CREDITOR_DEPOSIT_NUMBER_PREFIX + "\t" + depositBalances + "\n");
        printTransactionVOsToConsole(transactionVOS);
        return resultTransaction;
    }

    public static synchronized void writeTransactionVOToFile(List<TransactionVO> transactionVOS) throws IOException {
        PrintWriter printWriter = new PrintWriter(PaymentTransactionApp.TRANSACTION_FILE_PATH);
        for (TransactionVO transactionVO : transactionVOS) {
            printWriter.println(transactionVO.toString());
        }
        printWriter.close();
    }

    public static void printTransactionVOsToConsole(List<TransactionVO> transactionVOS) {
        System.out.println("******************** TRANSACTION ************************");
        for (TransactionVO transactionVO : transactionVOS)
            System.out.println(transactionVO.toString());
        System.out.println("*********************************************************");
    }
}

