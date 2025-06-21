package exceptions;

public class PasswordResetTokenException extends RuntimeException {
  public PasswordResetTokenException(String message) {
    super(message);
  }

  public PasswordResetTokenException(String message, Throwable cause) {
    super(message, cause);
  }

  public PasswordResetTokenException() {
    super();
  }
}
