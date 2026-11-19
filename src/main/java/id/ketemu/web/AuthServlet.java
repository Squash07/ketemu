package id.ketemu.web;

import id.ketemu.dao.UserDao;
import id.ketemu.model.User;
import id.ketemu.util.Flash;
import id.ketemu.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.Map;

/** Sign in, sign out and account creation. */
@WebServlet(urlPatterns = {"/login", "/logout", "/register"})
public class AuthServlet extends BaseServlet {

    private final UserDao users = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (me(req) != null && !"/logout".equals(req.getServletPath())) {
            redirect(req, res, "/");
            return;
        }
        switch (req.getServletPath()) {
            case "/login" -> {
                req.setAttribute("next", safeNext(req.getParameter("next")));
                render(req, res, "login");
            }
            case "/register" -> render(req, res, "register");
            default -> redirect(req, res, "/");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        switch (req.getServletPath()) {
            case "/login" -> login(req, res);
            case "/register" -> register(req, res);
            default -> logout(req, res);
        }
    }

    private void login(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String email = Validator.clean(req.getParameter("email"));
        String password = req.getParameter("password") == null ? "" : req.getParameter("password");
        String next = safeNext(req.getParameter("next"));
        req.setAttribute("next", next);
        req.setAttribute("email", email);

        Validator v = new Validator()
                .required("email", email, "Enter the email you signed up with.")
                .required("password", password, "Enter your password.");
        if (!v.ok()) {
            renderForm(req, res, "login", v.errors());
            return;
        }
        User u = users.findByEmail(email);
        if (u == null || u.getPasswordHash().startsWith("PENDING") || !BCrypt.checkpw(password, u.getPasswordHash())) {
            renderForm(req, res, "login", Map.of("password",
                    "That email and password do not match. Check for typos, or create an account if you are new."));
            return;
        }
        signIn(req, u);
        Flash.ok(req, "Signed in. Welcome back, " + u.getFirstName() + ".");
        redirect(req, res, next);
    }

    private void register(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User u = new User();
        u.setNrp(Validator.clean(req.getParameter("nrp")));
        u.setFullName(Validator.clean(req.getParameter("fullName")));
        u.setEmail(Validator.clean(req.getParameter("email")).toLowerCase());
        u.setPhone(Validator.orNull(req.getParameter("phone")));
        String password = req.getParameter("password") == null ? "" : req.getParameter("password");
        String confirm = req.getParameter("confirm") == null ? "" : req.getParameter("confirm");
        req.setAttribute("form", u);

        Validator v = new Validator()
                .required("nrp", u.getNrp(), "Enter your NRP so the finder knows who is claiming.")
                .nrp("nrp", u.getNrp())
                .required("fullName", u.getFullName(), "Enter your full name.")
                .maxLength("fullName", u.getFullName(), 100, "Your name")
                .required("email", u.getEmail(), "Enter an email address.")
                .email("email", u.getEmail())
                .maxLength("email", u.getEmail(), 120, "The email")
                .phone("phone", u.getPhone())
                .required("password", password, "Choose a password.")
                .minLength("password", password, 8, "Use at least 8 characters for the password.")
                .check(password.length() <= 72, "password", "Keep the password under 72 characters.")
                .required("confirm", confirm, "Type the password again.")
                .check(password.equals(confirm), "confirm", "The two passwords are different. Type the same one twice.");
        if (v.ok() && users.nrpTaken(u.getNrp(), 0)) {
            v.check(false, "nrp", "There is already an account with this NRP. Sign in instead.");
        }
        if (v.ok() && users.emailTaken(u.getEmail(), 0)) {
            v.check(false, "email", "There is already an account with this email. Sign in instead.");
        }
        if (!v.ok()) {
            renderForm(req, res, "register", v.errors());
            return;
        }
        u.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt(10)));
        int id = users.create(u);
        signIn(req, users.findById(id));
        Flash.ok(req, "Account created. You can post an item or file a claim now.");
        redirect(req, res, "/");
    }

    private void logout(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession s = req.getSession(false);
        if (s != null) {
            s.invalidate();
        }
        Flash.ok(req, "Signed out.");
        redirect(req, res, "/");
    }
}
