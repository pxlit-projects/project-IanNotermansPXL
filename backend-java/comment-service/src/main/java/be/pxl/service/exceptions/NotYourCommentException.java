package be.pxl.service.exceptions;

public class NotYourCommentException extends RuntimeException {
    public NotYourCommentException(String message) {
        super(message);
    }
}
