package ir.dotin.exception;

public class NoDepositFoundException extends Exception {
    public NoDepositFoundException() {
    }

    public NoDepositFoundException(String message) {
        super(message);
    }
}
