package ski.serwon.exception;

public class ValueOutOfBoundsException extends RuntimeException {
    public ValueOutOfBoundsException() {
    }

    public ValueOutOfBoundsException(String message) {
        super(message);
    }
}
