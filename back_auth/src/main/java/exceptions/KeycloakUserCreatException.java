package exceptions;

public class KeycloakUserCreatException extends RuntimeException {
    public KeycloakUserCreatException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeycloakUserCreatException() {
        super();
    }
}
