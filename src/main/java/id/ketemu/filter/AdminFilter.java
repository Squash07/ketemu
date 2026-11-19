package id.ketemu.filter;

import id.ketemu.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Only SKK desk staff (role admin) may open /admin pages. */
public class AdminFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        Object me = req.getAttribute("me");
        if (!(me instanceof User u) || !u.isAdmin()) {
            req.setAttribute("errorText", "The desk pages are for SKK staff. If you work at a post, ask an admin to change your role.");
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        chain.doFilter(req, res);
    }
}
