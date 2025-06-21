package exceptions;

/**
 * Исключение, которое выбрасывается, когда пользователь уже существует.
 */
public class UserAlreadyExistsException extends RuntimeException {

    /**
     * Конструктор исключения.
     *
     * @param message сообщение об ошибке
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
