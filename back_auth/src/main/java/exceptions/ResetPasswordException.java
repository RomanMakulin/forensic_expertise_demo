package exceptions;

public class ResetPasswordException extends RuntimeException {
    public ResetPasswordException(String message) {
        super(message);
    }

    public ResetPasswordException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResetPasswordException() {
        super();
    }
}
