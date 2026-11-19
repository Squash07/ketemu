package id.ketemu.web;

import id.ketemu.dao.ClaimDao;
import id.ketemu.dao.ItemDao;
import id.ketemu.dao.UserDao;
import id.ketemu.model.User;
import id.ketemu.util.Flash;
import id.ketemu.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;

/** Profile: view, edit details, change password, delete the account. */
@WebServlet(urlPatterns = {"/profile", "/profile/*"})
public class ProfileServlet extends BaseServlet {

    private final UserDao users = new UserDao();
    private final ItemDao items = new ItemDao();
    private final ClaimDao claims = new ClaimDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User me = requireUser(req);
        String[] p = parts(req);
        String action = p.length == 0 ? "" : p[0];
        switch (action) {
            case "" -> {
                ItemDao.Filter f = new ItemDao.Filter();
                f.reporterId = me.getId();
                f.includeClosed = true;
                req.setAttribute("myItems", items.search(f, 50, 0));
                req.setAttribute("myClaims", claims.listByClaimant(me.getId()));
                req.setAttribute("stats", users.stats(me.getId()));
                render(req, res, "profile/view");
            }
            case "edit" -> {
                req.setAttribute("form", me);
                render(req, res, "profile/edit");
            }
            case "password" -> render(req, res, "profile/password");
            case "delete" -> render(req, res, "profile/delete");
            default -> throw HttpError.notFound();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User me = requireUser(req);
        String[] p = parts(req);
        String action = p.length == 0 ? "" : p[0];
        switch (action) {
            case "edit" -> edit(req, res, me);
            case "password" -> password(req, res, me);
            case "delete" -> delete(req, res, me);
            default -> throw HttpError.notFound();
        }
    }

    private void edit(HttpServletRequest req, HttpServletResponse res, User me) throws ServletException, IOException {
        User u = new User();
        u.setId(me.getId());
        u.setNrp(Validator.clean(req.getParameter("nrp")));
        u.setFullName(Validator.clean(req.getParameter("fullName")));
        u.setEmail(Validator.clean(req.getParameter("email")).toLowerCase());
        u.setPhone(Validator.orNull(req.getParameter("phone")));
        req.setAttribute("form", u);
        Validator v = new Validator()
                .required("nrp", u.getNrp(), "Enter your NRP.")
                .nrp("nrp", u.getNrp())
                .required("fullName", u.getFullName(), "Enter your full name.")
                .maxLength("fullName", u.getFullName(), 100, "Your name")
                .required("email", u.getEmail(), "Enter an email address.")
                .email("email", u.getEmail())
                .phone("phone", u.getPhone());
        if (v.ok() && users.nrpTaken(u.getNrp(), me.getId())) {
            v.check(false, "nrp", "Another account already uses this NRP.");
        }
        if (v.ok() && users.emailTaken(u.getEmail(), me.getId())) {
            v.check(false, "email", "Another account already uses this email.");
        }
        if (!v.ok()) {
            renderForm(req, res, "profile/edit", v.errors());
            return;
        }
        users.updateProfile(u);
        Flash.ok(req, "Profile saved.");
        redirect(req, res, "/profile");
    }

    private void password(HttpServletRequest req, HttpServletResponse res, User me) throws ServletException, IOException {
        String current = req.getParameter("current") == null ? "" : req.getParameter("current");
        String next = req.getParameter("password") == null ? "" : req.getParameter("password");
        String confirm = req.getParameter("confirm") == null ? "" : req.getParameter("confirm");
        Validator v = new Validator()
                .required("current", current, "Enter your current password.")
                .check(current.isEmpty() || BCrypt.checkpw(current, me.getPasswordHash()), "current",
                        "That is not your current password. Try again.")
                .required("password", next, "Choose a new password.")
                .minLength("password", next, 8, "Use at least 8 characters for the new password.")
                .check(next.length() <= 72, "password", "Keep the password under 72 characters.")
                .check(next.equals(confirm), "confirm", "The two passwords are different. Type the same one twice.");
        if (!v.ok()) {
            renderForm(req, res, "profile/password", v.errors());
            return;
        }
        users.updatePassword(me.getId(), BCrypt.hashpw(next, BCrypt.gensalt(10)));
        req.changeSessionId();
        Flash.ok(req, "Password changed.");
        redirect(req, res, "/profile");
    }

    private void delete(HttpServletRequest req, HttpServletResponse res, User me) throws ServletException, IOException {
        String password = req.getParameter("password") == null ? "" : req.getParameter("password");
        Validator v = new Validator()
                .required("password", password, "Enter your password to confirm.")
                .check(password.isEmpty() || BCrypt.checkpw(password, me.getPasswordHash()), "password",
                        "That is not your password. Your account was not deleted.");
        if (v.ok() && me.isAdmin() && users.countAdmins() <= 1) {
            v.check(false, "password", "You are the last desk admin. Give another account the admin role first.");
        }
        if (!v.ok()) {
            renderForm(req, res, "profile/delete", v.errors());
            return;
        }
        users.delete(me.getId());
        req.getSession().invalidate();
        Flash.ok(req, "Account deleted. Your items, claims and comments were removed with it.");
        redirect(req, res, "/");
    }
}
