package id.ketemu.web;

import id.ketemu.dao.ClaimDao;
import id.ketemu.dao.ItemDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** The desk ledger: counts by status and the newest reports, in a plain table. */
@WebServlet(urlPatterns = {"/admin"})
public class AdminServlet extends BaseServlet {

    private final ItemDao items = new ItemDao();
    private final ClaimDao claims = new ClaimDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setAttribute("byStatus", items.countByStatus());
        req.setAttribute("openByKind", items.countOpenByKind());
        req.setAttribute("pendingClaims", claims.countPending());
        req.setAttribute("newest", items.newest(20));
        render(req, res, "admin/ledger");
    }
}
