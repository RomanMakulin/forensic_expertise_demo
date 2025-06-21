package exceptions;

public class UserCreateException extends RuntimeException {
    public UserCreateException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserCreateException() {
      super();
    }
}
