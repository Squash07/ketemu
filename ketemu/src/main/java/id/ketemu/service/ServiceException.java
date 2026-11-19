package id.ketemu.service;

/** A rule was broken. The message is written for the person using the site. */
public class ServiceException extends RuntimeException {

    private final int status;

    public ServiceException(int status, String message) {
        super(message);
        this.status = status;
    }

    public static ServiceException forbidden(String message) {
        return new ServiceException(403, message);
    }

    public static ServiceException conflict(String message) {
        return new ServiceException(409, message);
    }

    public int getStatus() {
        return status;
    }
}
