package ir.dotin.files;

import ir.dotin.PaymentTransactionApp;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class BalanceFileHandler {


    public static void createInitialBalanceFile(int creditorCount) throws IOException {
        System.out.println("Creating initial balance file...");
        List<BalanceVO> balanceVOs = new ArrayList<>();
        balanceVOs.add(new BalanceVO(PaymentTransactionApp.DEBTOR_DEPOSIT_NUMBER, PaymentTransactionApp.DEBTOR_DEPOSIT_AMOUNT));
        for (int i = 1; i <= creditorCount; i++) {
            balanceVOs.add(new BalanceVO(PaymentTransactionApp.CREDITOR_DEPOSIT_NUMBER_PREFIX + i, PaymentTransactionApp.generateRandomAmount()));
        }
        writeBalanceVOToFile(balanceVOs);
        Files.copy(Paths.get(PaymentTransactionApp.BALANCE_FILE_PATH), Paths.get(PaymentTransactionApp.BALANCE_UPDATE_FILE_PATH));
    }

    public static void writeBalanceVOToFile(List<BalanceVO> balanceVOs) throws IOException {
        PrintWriter printWriter = new PrintWriter(PaymentTransactionApp.BALANCE_FILE_PATH);
        for (BalanceVO balanceVO : balanceVOs) {
            printWriter.println(balanceVO.toString());
        }
        printWriter.close();
    }

}