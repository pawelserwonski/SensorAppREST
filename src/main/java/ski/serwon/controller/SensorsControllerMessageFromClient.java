package ski.serwon.controller;

public class SensorsControllerMessageFromClient {
    private String operation;
    private String value;

    int getValueParsedToInt() {
        return Integer.parseInt(value);
    }

    public SensorsControllerMessageFromClient() {
    }

    public SensorsControllerMessageFromClient(String operation, String value) {
        this.operation = operation;
        this.value = value;
    }

    public String getOperation() {
        return operation;
    }

    public String getValue() {
        return value;
    }
}
