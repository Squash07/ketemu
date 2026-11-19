package id.ketemu.filter;

import id.ketemu.util.Csrf;
import id.ketemu.util.Flash;
import id.ketemu.util.Uploads;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Synchronizer-token CSRF protection. Every page gets a session token, every
 * form posts it back as _csrf, and a POST without the right token is refused.
 */
public class CsrfFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        String path = req.getServletPath();
        if (path.startsWith("/assets/") || path.startsWith("/photo/")) {
            chain.doFilter(req, res);
            return;
        }
        HttpSession session = req.getSession();
        String expected = Csrf.token(session);
        if ("POST".equalsIgnoreCase(req.getMethod())) {
            String given = req.getParameter(Csrf.FIELD);
            if (given == null) {
                given = req.getHeader("X-CSRF-Token");
            }
            if (given == null && Uploads.isMultipart(req) && uploadTooLarge(req)) {
                Flash.error(req, "One of the photos is larger than 1 MB, so nothing was saved. Resize it and try again.");
                String back = req.getHeader("Referer");
                res.sendRedirect(back != null && back.contains("://" + req.getServerName()) ? back : req.getContextPath() + "/");
                return;
            }
            if (!Csrf.matches(expected, given)) {
                req.setAttribute("errorTitle", "This form expired");
                req.setAttribute("errorText", "The page was open for a long time or in another tab. Go back, reload the page and send the form again.");
                res.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }
        chain.doFilter(req, res);
    }

    /** Tomcat refuses to parse a multipart body whose file is over the limit; detect that case. */
    private static boolean uploadTooLarge(HttpServletRequest req) {
        try {
            req.getParts();
            return false;
        } catch (IllegalStateException e) {
            return true;
        } catch (IOException | ServletException e) {
            return false;
        }
    }
}
