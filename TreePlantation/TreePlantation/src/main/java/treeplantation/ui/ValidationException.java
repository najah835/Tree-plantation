package treeplantation.ui;

/** Thrown when the user's input is missing or invalid. The message is shown to the user. */
final class ValidationException extends Exception {
    ValidationException(String message) {
        super(message);
    }
}
