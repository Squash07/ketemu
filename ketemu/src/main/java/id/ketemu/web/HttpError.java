package id.ketemu.web;

/** Thrown inside a servlet to end the request with an error page. */
public class HttpError extends RuntimeException {

    private final int status;

    public HttpError(int status, String message) {
        super(message);
        this.status = status;
    }

    public static HttpError notFound() {
        return new HttpError(404, null);
    }

    public int getStatus() {
        return status;
    }
}
