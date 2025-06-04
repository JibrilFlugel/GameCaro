package game.caro.classes;

public enum MessageType {
    MOVE("MOVE", true),
    TURN_SWITCH("TURN_SWITCH", false);

    private final String value;
    private final boolean hasParams;

    MessageType(String value, boolean hasParams) {
        this.value = value;
        this.hasParams = hasParams;
    }

    public String getValue() {
        return value;
    }

    public boolean hasParameters() {
        return hasParams;
    }

    public String serialize(int... params) {
        if (!hasParams && params.length > 0) {
            throw new IllegalArgumentException();
        }
        if (hasParams && params.length != 2) {
            throw new IllegalArgumentException();
        }
        if (hasParams) {
            return value + " " + params[0] + " " + params[1];
        }
        return value;
    }

    public static MessageType parse(String message) throws IllegalArgumentException {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        String[] parts = message.split(" ");
        for (MessageType type : values()) {
            if (parts[0].equals(type.value)) {
                if (type.hasParams && parts.length != 3) {
                    throw new IllegalArgumentException();
                }
                if (!type.hasParams && parts.length != 1) {
                    throw new IllegalArgumentException();
                }
                return type;
            }
        }
        throw new IllegalArgumentException();
    }

    public static int[] getParameters(String message) {
        String[] parts = message.split(" ");
        if (parts.length != 3) {
            throw new IllegalArgumentException();
        }
        try {
            return new int[] { Integer.parseInt(parts[1]), Integer.parseInt(parts[2]) };
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException();
        }
    }
}
