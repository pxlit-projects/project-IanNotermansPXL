package be.pxl.service.exceptions;

public class NotYourPostException extends RuntimeException {
    public NotYourPostException(String message) {
        super(message);
    }
}
