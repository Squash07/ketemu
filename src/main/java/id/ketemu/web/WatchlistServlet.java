package id.ketemu.web;

import id.ketemu.dao.ItemDao;
import id.ketemu.dao.WatchlistDao;
import id.ketemu.model.Item;
import id.ketemu.model.User;
import id.ketemu.util.Flash;
import id.ketemu.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Saved items with a private note. You get a notification when a saved item changes. */
@WebServlet(urlPatterns = {"/watchlist", "/watchlist/*"})
public class WatchlistServlet extends BaseServlet {

    private final WatchlistDao watchlist = new WatchlistDao();
    private final ItemDao items = new ItemDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User me = requireUser(req);
        if (parts(req).length > 0) {
            throw HttpError.notFound();
        }
        req.setAttribute("entries", watchlist.listByUser(me.getId()));
        render(req, res, "watchlist");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User me = requireUser(req);
        String[] p = parts(req);
        Integer itemId = intParam(req, "itemId");
        Item item = found(itemId == null ? null : items.findById(itemId));
        switch (p.length > 0 ? p[0] : "") {
            case "add" -> {
                watchlist.add(me.getId(), item.getId(), null);
                Flash.ok(req, "Saved to watchlist. We'll tell you when " + item.getCode() + " changes.");
                redirect(req, res, "/items/" + item.getId());
            }
            case "note" -> {
                String note = Validator.orNull(req.getParameter("note"));
                if (note != null && note.length() > 140) {
                    Flash.error(req, "Keep the note under 140 characters. It was not saved.");
                } else if (watchlist.updateNote(me.getId(), item.getId(), note) == 0) {
                    Flash.error(req, "That item is not in your watchlist any more.");
                } else {
                    Flash.ok(req, note == null ? "Note cleared." : "Note saved.");
                }
                redirect(req, res, "/watchlist#w-" + item.getId());
            }
            case "remove" -> {
                watchlist.remove(me.getId(), item.getId());
                Flash.ok(req, "Removed from watchlist.");
                redirect(req, res, safeNext(req.getParameter("back")).equals("/") ? "/watchlist" : safeNext(req.getParameter("back")));
            }
            default -> throw HttpError.notFound();
        }
    }
}
