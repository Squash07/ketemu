package id.ketemu.web;

import id.ketemu.dao.UserDao;
import id.ketemu.model.User;
import id.ketemu.util.Flash;
import id.ketemu.util.Paging;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Desk: list and search users, change a role, remove an account. */
@WebServlet(urlPatterns = {"/admin/users", "/admin/users/*"})
public class AdminUserServlet extends BaseServlet {

    private final UserDao users = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (parts(req).length > 0) {
            throw HttpError.notFound();
        }
        String q = req.getParameter("q") == null ? "" : req.getParameter("q").trim();
        Paging paging = new Paging(req.getParameter("page"), 20, users.countSearch(q));
        req.setAttribute("users", users.search(q, paging.getSize(), paging.getOffset()));
        req.setAttribute("paging", paging);
        req.setAttribute("q", q);
        req.setAttribute("pageBase", q.isEmpty() ? "" : "?q=" + URLEncoder.encode(q, StandardCharsets.UTF_8));
        render(req, res, "admin/users");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User me = requireUser(req);
        String[] p = parts(req);
        User target = found(users.findById(idAt(p, 0)));
        switch (p.length > 1 ? p[1] : "") {
            case "role" -> {
                String role = req.getParameter("role");
                if (!"admin".equals(role) && !"student".equals(role)) {
                    Flash.error(req, "Pick student or admin.");
                } else if (target.getId() == me.getId() && "student".equals(role)) {
                    Flash.error(req, "You cannot remove your own admin role. Ask another admin to do it.");
                } else {
                    users.updateRole(target.getId(), role);
                    Flash.ok(req, "Role changed. " + target.getFullName() + " is now " + ("admin".equals(role) ? "a desk admin." : "a student."));
                }
            }
            case "delete" -> {
                if (target.getId() == me.getId()) {
                    Flash.error(req, "You cannot remove your own account here. Use Delete account on your profile.");
                } else {
                    users.delete(target.getId());
                    Flash.ok(req, "Account removed. " + target.getFullName() + "'s items, claims and comments went with it.");
                }
            }
            default -> throw HttpError.notFound();
        }
        redirect(req, res, "/admin/users");
    }
}
