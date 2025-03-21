package client;

public class ResponseException extends Exception {
    final private int status;

    public ResponseException(int statusCode, String message) {
        super(message);
        this.status = statusCode;
    }

    public int statusCode() {
        return status;
    }
}