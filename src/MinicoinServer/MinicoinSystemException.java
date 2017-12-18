package MinicoinServer;

public class MinicoinSystemException extends Exception {
    public MinicoinSystemException() {
    }

    public MinicoinSystemException(String message) {
        super(message);
    }

    public MinicoinSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public MinicoinSystemException(Throwable cause) {
        super(cause);
    }
}
