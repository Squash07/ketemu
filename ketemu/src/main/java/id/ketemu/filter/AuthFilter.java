package id.ketemu.filter;

import id.ketemu.dao.DataException;
import id.ketemu.dao.NotificationDao;
import id.ketemu.dao.UserDao;
import id.ketemu.model.User;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Loads the signed-in user on every request (so role changes and deleted
 * accounts take effect at once) and sends guests to the sign-in page when
 * they open a page that needs an account.
 */
public class AuthFilter extends HttpFilter {

    public static final String SESSION_USER_ID = "userId";

    private static final String[] PROTECTED = {
        "/profile", "/claims", "/watchlist", "/notifications", "/admin", "/photos", "/comments", "/items/new"
    };

    private final UserDao users = new UserDao();
    private final NotificationDao notifications = new NotificationDao();

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        String path = req.getServletPath() + (req.getPathInfo() == null ? "" : req.getPathInfo());
        if (path.startsWith("/assets/") || path.startsWith("/photo/")) {
            chain.doFilter(req, res);
            return;
        }

        HttpSession session = req.getSession(false);
        User me = null;
        if (session != null && session.getAttribute(SESSION_USER_ID) instanceof Integer id) {
            try {
                me = users.findById(id);
                if (me == null) {
                    session.removeAttribute(SESSION_USER_ID);
                } else {
                    req.setAttribute("me", me);
                    req.setAttribute("unread", notifications.countUnread(me.getId()));
                }
            } catch (DataException e) {
                if (req.getDispatcherType() != DispatcherType.ERROR) {
                    throw e;
                }
            }
        }

        if (me == null && isProtected(path)) {
            String next = "GET".equalsIgnoreCase(req.getMethod())
                    ? path + (req.getQueryString() == null ? "" : "?" + req.getQueryString())
                    : "/";
            res.sendRedirect(req.getContextPath() + "/login?next=" + URLEncoder.encode(next, StandardCharsets.UTF_8));
            return;
        }
        chain.doFilter(req, res);
    }

    private static boolean isProtected(String path) {
        for (String p : PROTECTED) {
            if (path.equals(p) || path.startsWith(p + "/")) {
                return true;
            }
        }
        return false;
    }
}
