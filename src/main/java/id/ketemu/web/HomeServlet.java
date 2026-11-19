package id.ketemu.web;

import id.ketemu.dao.CategoryDao;
import id.ketemu.dao.ItemDao;
import id.ketemu.dao.LocationDao;
import id.ketemu.util.Paging;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/** Home page: hero, the board with filters and search, and the handover steps. */
@WebServlet(urlPatterns = {""})
public class HomeServlet extends BaseServlet {

    private static final int PAGE_SIZE = 12;

    private final ItemDao items = new ItemDao();
    private final CategoryDao categories = new CategoryDao();
    private final LocationDao locations = new LocationDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        ItemDao.Filter f = new ItemDao.Filter();
        String kind = req.getParameter("kind");
        f.kind = "lost".equals(kind) || "found".equals(kind) ? kind : null;
        f.categoryId = intParam(req, "category");
        f.locationId = intParam(req, "place");
        String q = req.getParameter("q");
        f.q = q == null ? null : q.trim();
        if (f.q != null && f.q.length() > 80) {
            f.q = f.q.substring(0, 80);
        }

        Paging paging = new Paging(req.getParameter("page"), PAGE_SIZE, items.countSearch(f));
        req.setAttribute("items", items.search(f, paging.getSize(), paging.getOffset()));
        req.setAttribute("paging", paging);
        req.setAttribute("counts", items.countByKind(f));
        req.setAttribute("filter", f);
        req.setAttribute("categories", categories.listAll());
        req.setAttribute("locations", locations.listAll());
        req.setAttribute("returnedThisMonth", items.countReturnedSince(LocalDate.now().withDayOfMonth(1)));
        req.setAttribute("filtered", f.kind != null || f.categoryId != null || f.locationId != null || (f.q != null && !f.q.isEmpty()));

        StringBuilder rest = new StringBuilder();
        if (f.categoryId != null) rest.append("&category=").append(f.categoryId);
        if (f.locationId != null) rest.append("&place=").append(f.locationId);
        if (f.q != null && !f.q.isEmpty()) rest.append("&q=").append(URLEncoder.encode(f.q, StandardCharsets.UTF_8));
        req.setAttribute("segAll", rest.length() == 0 ? "" : "?" + rest.substring(1));
        req.setAttribute("segLost", "?kind=lost" + rest);
        req.setAttribute("segFound", "?kind=found" + rest);

        StringBuilder base = new StringBuilder("?");
        if (f.kind != null) base.append("kind=").append(f.kind).append('&');
        if (f.categoryId != null) base.append("category=").append(f.categoryId).append('&');
        if (f.locationId != null) base.append("place=").append(f.locationId).append('&');
        if (f.q != null && !f.q.isEmpty()) base.append("q=").append(URLEncoder.encode(f.q, StandardCharsets.UTF_8)).append('&');
        String b = base.toString();
        req.setAttribute("pageBase", b.endsWith("&") || b.endsWith("?") ? b.substring(0, b.length() - 1) : b);
        render(req, res, "home");
    }
}
