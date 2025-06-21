package exceptions;

/**
 * Исключение, которое выбрасывается, когда пользователь Keycloak не найден.
 */
public class CannotFindKeycloakUserException extends RuntimeException {

    /**
     * Конструктор исключения NoKeycloakUserFound.
     *
     * @param message сообщение об ошибке
     */
    public CannotFindKeycloakUserException(String message) {
        super(message);
    }
}
