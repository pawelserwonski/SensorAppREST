package ski.serwon.exception;

public class SensorNotFoundException extends RuntimeException {
    public SensorNotFoundException() {
    }

    public SensorNotFoundException(String message) {
        super(message);
    }
}
