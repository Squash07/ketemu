package id.ketemu.web;

import id.ketemu.dao.NotificationDao;
import id.ketemu.model.User;
import id.ketemu.util.Flash;
import id.ketemu.util.Paging;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Notification list, mark read, mark all read, delete one, clear all. */
@WebServlet(urlPatterns = {"/notifications", "/notifications/*"})
public class NotificationServlet extends BaseServlet {

    private final NotificationDao notifications = new NotificationDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User me = requireUser(req);
        if (parts(req).length > 0) {
            throw HttpError.notFound();
        }
        Paging paging = new Paging(req.getParameter("page"), 20, notifications.countByUser(me.getId()));
        req.setAttribute("notes", notifications.listByUser(me.getId(), paging.getSize(), paging.getOffset()));
        req.setAttribute("paging", paging);
        render(req, res, "notifications");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User me = requireUser(req);
        String[] p = parts(req);
        String action = p.length == 0 ? "" : p[0];
        switch (action) {
            case "read-all" -> {
                notifications.markAllRead(me.getId());
                Flash.ok(req, "Marked all as read.");
            }
            case "clear" -> {
                notifications.clear(me.getId());
                Flash.ok(req, "Notifications cleared.");
            }
            default -> {
                int id = idAt(p, 0);
                String sub = p.length > 1 ? p[1] : "";
                if ("read".equals(sub)) {
                    if (notifications.markRead(id, me.getId()) == 0) {
                        throw HttpError.notFound();
                    }
                    String go = req.getParameter("go");
                    if (go != null && go.matches("/items/\\d+")) {
                        redirect(req, res, go);
                        return;
                    }
                    Flash.ok(req, "Marked as read.");
                } else if ("delete".equals(sub)) {
                    if (notifications.delete(id, me.getId()) == 0) {
                        throw HttpError.notFound();
                    }
                    Flash.ok(req, "Notification deleted.");
                } else {
                    throw HttpError.notFound();
                }
            }
        }
        redirect(req, res, "/notifications");
    }
}
