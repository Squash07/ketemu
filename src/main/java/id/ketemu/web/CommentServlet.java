package id.ketemu.web;

import id.ketemu.dao.CommentDao;
import id.ketemu.dao.ItemDao;
import id.ketemu.model.Comment;
import id.ketemu.model.Item;
import id.ketemu.model.User;
import id.ketemu.service.NotificationService;
import id.ketemu.service.ServiceException;
import id.ketemu.util.Flash;
import id.ketemu.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Sightings and comments on an item: add, edit your own, delete your own (admins can delete any). */
@WebServlet(urlPatterns = {"/comments", "/comments/*"})
public class CommentServlet extends BaseServlet {

    private final CommentDao comments = new CommentDao();
    private final ItemDao items = new ItemDao();
    private final NotificationService notes = new NotificationService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User me = requireUser(req);
        String[] p = parts(req);
        if (p.length == 0) {
            create(req, res, me);
            return;
        }
        Comment c = found(comments.findById(idAt(p, 0)));
        String back = "/items/" + c.getItemId() + "#comment-" + c.getId();
        switch (p.length > 1 ? p[1] : "") {
            case "edit" -> {
                if (c.getAuthorId() != me.getId()) {
                    throw ServiceException.forbidden("You can only edit your own comments.");
                }
                String body = Validator.clean(req.getParameter("body"));
                Validator v = new Validator()
                        .required("body", body, "A comment cannot be empty. Delete it instead if you want it gone.")
                        .maxLength("body", body, 1000, "The comment");
                if (!v.ok()) {
                    Flash.error(req, v.errors().get("body"));
                } else {
                    comments.update(c.getId(), body);
                    Flash.ok(req, "Comment saved.");
                }
                redirect(req, res, back);
            }
            case "delete" -> {
                if (c.getAuthorId() != me.getId() && !me.isAdmin()) {
                    throw ServiceException.forbidden("You can only delete your own comments.");
                }
                comments.delete(c.getId());
                Flash.ok(req, "Comment deleted.");
                redirect(req, res, "/items/" + c.getItemId() + "#comments-title");
            }
            default -> throw HttpError.notFound();
        }
    }

    private void create(HttpServletRequest req, HttpServletResponse res, User me) throws IOException {
        Integer itemId = intParam(req, "itemId");
        Item item = found(itemId == null ? null : items.findById(itemId));
        String body = Validator.clean(req.getParameter("body"));
        Validator v = new Validator()
                .required("body", body, "Write something first, like where and when you saw it.")
                .maxLength("body", body, 1000, "The comment");
        if (!v.ok()) {
            req.getSession().setAttribute("commentDraft", body);
            req.getSession().setAttribute("commentError", v.errors().get("body"));
            redirect(req, res, "/items/" + item.getId() + "#new-comment");
            return;
        }
        int id = comments.create(item.getId(), me.getId(), body);
        if (item.getReporterId() != me.getId()) {
            notes.notify(item.getReporterId(), item.getId(), me.getFullName() + " commented on your item " + item.getCode() + ".");
        }
        notes.notifyWatchers(item.getId(), me.getFullName() + " commented on " + item.getCode() + ", an item in your watchlist.",
                me.getId(), item.getReporterId());
        Flash.ok(req, "Comment posted.");
        redirect(req, res, "/items/" + item.getId() + "#comment-" + id);
    }
}
