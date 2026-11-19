package id.ketemu.util;

import jakarta.servlet.http.HttpSession;

import java.security.SecureRandom;
import java.util.Base64;

/** Per-session token that every POST form sends back in the hidden field _csrf. */
public final class Csrf {

    public static final String KEY = "csrf";
    public static final String FIELD = "_csrf";
    private static final SecureRandom RANDOM = new SecureRandom();

    private Csrf() {
    }

    public static String token(HttpSession session) {
        Object t = session.getAttribute(KEY);
        if (t == null) {
            byte[] b = new byte[32];
            RANDOM.nextBytes(b);
            t = Base64.getUrlEncoder().withoutPadding().encodeToString(b);
            session.setAttribute(KEY, t);
        }
        return (String) t;
    }

    /** Constant-time comparison. */
    public static boolean matches(String expected, String given) {
        if (expected == null || given == null || expected.length() != given.length()) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < expected.length(); i++) {
            diff |= expected.charAt(i) ^ given.charAt(i);
        }
        return diff == 0;
    }
}
