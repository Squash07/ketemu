package id.ketemu.web;

import id.ketemu.filter.AuthFilter;
import id.ketemu.model.User;
import id.ketemu.service.ServiceException;
import id.ketemu.util.Flash;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Small helpers shared by every controller. */
public abstract class BaseServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            super.service(req, res);
        } catch (LoginRequired e) {
            String next = req.getServletPath() + (req.getPathInfo() == null ? "" : req.getPathInfo());
            res.sendRedirect(req.getContextPath() + "/login?next=" + URLEncoder.encode(next, StandardCharsets.UTF_8));
        } catch (HttpError e) {
            if (e.getMessage() != null) {
                req.setAttribute("errorText", e.getMessage());
            }
            res.sendError(e.getStatus());
        } catch (ServiceException e) {
            if (e.getStatus() == 403 || e.getStatus() == 404) {
                req.setAttribute("errorText", e.getMessage());
                res.sendError(e.getStatus());
            } else {
                Flash.error(req, e.getMessage());
                String back = req.getHeader("Referer");
                res.sendRedirect(back != null && isSameOrigin(req, back) ? back : req.getContextPath() + "/");
            }
        }
    }

    private static boolean isSameOrigin(HttpServletRequest req, String url) {
        String host = req.getServerName();
        return url.startsWith("/") && !url.startsWith("//") || url.contains("://" + host);
    }

    protected static class LoginRequired extends RuntimeException {
    }

    protected User me(HttpServletRequest req) {
        return (User) req.getAttribute("me");
    }

    protected User requireUser(HttpServletRequest req) {
        User u = me(req);
        if (u == null) {
            throw new LoginRequired();
        }
        return u;
    }

    protected void signIn(HttpServletRequest req, User user) {
        req.getSession(true);
        req.changeSessionId();
        req.getSession().setAttribute(AuthFilter.SESSION_USER_ID, user.getId());
        req.setAttribute("me", user);
    }

    protected void render(HttpServletRequest req, HttpServletResponse res, String view) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp").forward(req, res);
    }

    protected void renderForm(HttpServletRequest req, HttpServletResponse res, String view, Map<String, String> errors)
            throws ServletException, IOException {
        req.setAttribute("errors", errors);
        res.setStatus(422);
        render(req, res, view);
    }

    protected void redirect(HttpServletRequest req, HttpServletResponse res, String path) throws IOException {
        res.sendRedirect(req.getContextPath() + path);
    }

    /** Path segments after the servlet mapping: /items/12/edit gives ["12", "edit"]. */
    protected String[] parts(HttpServletRequest req) {
        String info = req.getPathInfo();
        if (info == null || info.equals("/")) {
            return new String[0];
        }
        return info.substring(1).split("/");
    }

    protected int idAt(String[] parts, int index) {
        if (parts.length <= index) {
            throw HttpError.notFound();
        }
        try {
            return Integer.parseInt(parts[index]);
        } catch (NumberFormatException e) {
            throw HttpError.notFound();
        }
    }

    protected static Integer intParam(HttpServletRequest req, String name) {
        try {
            String v = req.getParameter(name);
            return v == null || v.isBlank() ? null : Integer.valueOf(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected static <T> T found(T value) {
        if (value == null) {
            throw HttpError.notFound();
        }
        return value;
    }

    /** Keeps redirects on this site. */
    protected static String safeNext(String next) {
        if (next == null || !next.startsWith("/") || next.startsWith("//") || next.contains("\\")) {
            return "/";
        }
        return next;
    }
}
