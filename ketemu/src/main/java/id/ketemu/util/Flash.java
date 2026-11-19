package id.ketemu.util;

import jakarta.servlet.http.HttpServletRequest;

import java.io.Serializable;

/** One-time message shown after a redirect (Post/Redirect/Get). */
public class Flash implements Serializable {

    private static final String KEY = "flash";

    private final String message;
    private final String type;
    private final boolean stamp;

    private Flash(String message, String type, boolean stamp) {
        this.message = message;
        this.type = type;
        this.stamp = stamp;
    }

    public static void ok(HttpServletRequest req, String message) {
        req.getSession().setAttribute(KEY, new Flash(message, "ok", false));
    }

    /** Success message that also plays the stamp-press animation on the page it lands on. */
    public static void stamped(HttpServletRequest req, String message) {
        req.getSession().setAttribute(KEY, new Flash(message, "ok", true));
    }

    public static void error(HttpServletRequest req, String message) {
        req.getSession().setAttribute(KEY, new Flash(message, "error", false));
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public boolean isStamp() {
        return stamp;
    }
}
