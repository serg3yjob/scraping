package ru.scraping.data.exceptions;

public class ServiceException extends RuntimeException {

    public ServiceException(String msg, Exception cause) {
        super(msg, cause);
    }

    public ServiceException(Exception cause) {
        super(cause);
    }
}
