package ir.dotin.exception;

public class InadequateInitialBalanceException extends Exception {
    public InadequateInitialBalanceException() {
    }

    public InadequateInitialBalanceException(String message) {
        super(message);
    }
}
