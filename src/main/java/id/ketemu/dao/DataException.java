package id.ketemu.dao;

/** Unchecked wrapper so servlets do not have to deal with SQLException directly. */
public class DataException extends RuntimeException {

    public DataException(String message, Throwable cause) {
        super(message, cause);
    }
}
