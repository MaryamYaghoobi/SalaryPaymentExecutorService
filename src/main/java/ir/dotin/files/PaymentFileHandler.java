package ir.dotin.files;

import ir.dotin.PaymentTransactionApp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static ir.dotin.business.DepositType.CREDITOR;
import static ir.dotin.business.DepositType.DEBTOR;

public class PaymentFileHandler {

    public static List<PaymentVO> createPaymentFile(String debtorDepositNumber, String creditorDepositNumberPrefix, int creditorCount) throws IOException {
        System.out.println("Creating payment file...");
        List<PaymentVO> paymentVOs = new ArrayList<>();
        paymentVOs.add(new PaymentVO(DEBTOR, debtorDepositNumber, PaymentTransactionApp.DEBTOR_DEPOSIT_AMOUNT));
        for (int i = 1; i <= creditorCount; i++) {
            paymentVOs.add(new PaymentVO(CREDITOR, creditorDepositNumberPrefix + i, PaymentTransactionApp.generateRandomAmount()));
        }
        writePaymentRecordsToFile(paymentVOs);
        return paymentVOs;
    }

    public static void writePaymentRecordsToFile(List<PaymentVO> paymentVOs) throws IOException {
        PrintWriter printWriter = new PrintWriter(PaymentTransactionApp.PAYMENT_FILE_PATH);
        for (PaymentVO paymentVO : paymentVOs) {
            printWriter.println(paymentVO.toString());
        }
        printWriter.close();
    }

}