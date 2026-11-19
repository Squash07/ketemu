package id.ketemu.web;

import id.ketemu.dao.CategoryDao;
import id.ketemu.dao.ClaimDao;
import id.ketemu.dao.CommentDao;
import id.ketemu.dao.ItemDao;
import id.ketemu.dao.LocationDao;
import id.ketemu.dao.PhotoDao;
import id.ketemu.dao.Tx;
import id.ketemu.dao.UserDao;
import id.ketemu.dao.WatchlistDao;
import id.ketemu.model.Claim;
import id.ketemu.model.Item;
import id.ketemu.model.ItemPhoto;
import id.ketemu.model.User;
import id.ketemu.service.ClaimService;
import id.ketemu.service.ItemService;
import id.ketemu.util.Flash;
import id.ketemu.util.Uploads;
import id.ketemu.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Item reports: create, detail, edit, delete and status changes.
 * Also takes new claims (/items/{id}/claim) because the claim form lives on the item page.
 */
@WebServlet(urlPatterns = {"/items/*"})
@MultipartConfig(maxFileSize = 1_048_576, maxRequestSize = 4 * 1_048_576, fileSizeThreshold = 0)
public class ItemServlet extends BaseServlet {

    private final ItemDao items = new ItemDao();
    private final PhotoDao photos = new PhotoDao();
    private final CategoryDao categories = new CategoryDao();
    private final LocationDao locations = new LocationDao();
    private final ClaimDao claims = new ClaimDao();
    private final CommentDao comments = new CommentDao();
    private final WatchlistDao watchlist = new WatchlistDao();
    private final UserDao users = new UserDao();
    private final ItemService itemService = new ItemService();
    private final ClaimService claimService = new ClaimService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String[] p = parts(req);
        if (p.length == 0) {
            redirect(req, res, "/#board");
            return;
        }
        if ("new".equals(p[0])) {
            requireUser(req);
            Item blank = new Item();
            String kind = req.getParameter("kind");
            blank.setKind("found".equals(kind) ? "found" : "lost");
            blank.setTitle(Validator.clean(req.getParameter("title")));
            blank.setEventDate(java.time.LocalDate.now());
            req.setAttribute("form", blank);
            showForm(req, res, false);
            return;
        }
        Item item = found(items.findById(idAt(p, 0)));
        String action = p.length > 1 ? p[1] : "";
        switch (action) {
            case "" -> detail(req, res, item);
            case "edit" -> {
                itemService.requireReporter(item, requireUser(req));
                req.setAttribute("form", item);
                showForm(req, res, true);
            }
            default -> throw HttpError.notFound();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User me = requireUser(req);
        String[] p = parts(req);
        if (p.length == 1 && "new".equals(p[0])) {
            save(req, res, me, null);
            return;
        }
        Item item = found(items.findById(idAt(p, 0)));
        String action = p.length > 1 ? p[1] : "";
        switch (action) {
            case "edit" -> save(req, res, me, item);
            case "delete" -> {
                itemService.delete(item, me);
                Flash.ok(req, "Report deleted. " + item.getCode() + " is off the board.");
                redirect(req, res, me.getId() == item.getReporterId() ? "/profile" : "/");
            }
            case "returned" -> {
                claimService.markReturned(item, me);
                Flash.stamped(req, "Marked returned.");
                redirect(req, res, "/items/" + item.getId());
            }
            case "recovered" -> {
                itemService.markRecovered(item, me);
                Flash.stamped(req, "Marked as back with you.");
                redirect(req, res, "/items/" + item.getId());
            }
            case "close" -> {
                itemService.close(item, me);
                Flash.ok(req, "Report closed. It no longer shows on the board.");
                redirect(req, res, "/items/" + item.getId());
            }
            case "reopen" -> {
                itemService.reopen(item, me);
                Flash.ok(req, "Report reopened. It is back on the board.");
                redirect(req, res, "/items/" + item.getId());
            }
            case "claim" -> fileClaim(req, res, me, item);
            default -> throw HttpError.notFound();
        }
    }

    private void detail(HttpServletRequest req, HttpServletResponse res, Item item) throws ServletException, IOException {
        User me = me(req);
        List<ItemPhoto> gallery = photos.listByItem(item.getId());
        Integer shown = intParam(req, "photo");
        ItemPhoto current = gallery.isEmpty() ? null : gallery.get(0);
        for (ItemPhoto ph : gallery) {
            if (shown != null && ph.getId() == shown) {
                current = ph;
            }
        }
        boolean isReporter = ItemService.isReporter(item, me);
        req.setAttribute("item", item);
        req.setAttribute("photos", gallery);
        req.setAttribute("currentPhoto", current);
        req.setAttribute("comments", comments.listByItem(item.getId()));
        req.setAttribute("isReporter", isReporter);
        req.setAttribute("reporterStats", users.stats(item.getReporterId()));
        if (me != null) {
            req.setAttribute("watching", watchlist.isWatching(me.getId(), item.getId()));
            req.setAttribute("myClaim", claims.activeFor(item.getId(), me.getId()));
        }
        if (isReporter && item.isFound()) {
            req.setAttribute("incoming", claims.listByItem(item.getId()));
        }
        if (me != null && !isReporter && req.getAttribute("claimBlock") == null) {
            req.setAttribute("claimBlock", claimService.whyCannotClaim(item, me));
        }
        if (item.isFound()) {
            req.setAttribute("posts", locations.listPosts());
        }
        render(req, res, "items/detail");
    }

    private void showForm(HttpServletRequest req, HttpServletResponse res, boolean editing) throws ServletException, IOException {
        req.setAttribute("editing", editing);
        req.setAttribute("categories", categories.listAll());
        req.setAttribute("locations", locations.listAll());
        req.setAttribute("posts", locations.listPosts());
        Item form = (Item) req.getAttribute("form");
        if (editing && form != null) {
            req.setAttribute("photos", photos.listByItem(form.getId()));
        }
        render(req, res, "items/form");
    }

    private void save(HttpServletRequest req, HttpServletResponse res, User me, Item existing) throws ServletException, IOException {
        boolean editing = existing != null;
        if (editing) {
            itemService.requireReporter(existing, me);
        }
        Item it = new Item();
        if (editing) {
            it.setId(existing.getId());
            it.setCode(existing.getCode());
            it.setKind(existing.getKind());
            it.setStatus(existing.getStatus());
        } else {
            String kind = req.getParameter("kind");
            it.setKind("found".equals(kind) ? "found" : "lost".equals(kind) ? "lost" : null);
        }
        it.setReporterId(me.getId());
        it.setTitle(Validator.clean(req.getParameter("title")));
        it.setDescription(Validator.orNull(req.getParameter("description")));
        it.setLocationDetail(Validator.orNull(req.getParameter("locationDetail")));

        Validator v = new Validator();
        v.check(it.getKind() != null, "kind", "Choose whether you lost or found this item.");
        v.required("title", it.getTitle(), "Give the item a short name, like “Black wallet with KTM”.")
                .minLength("title", it.getTitle(), 3, "Add a few more words so people can recognise it.")
                .maxLength("title", it.getTitle(), 80, "The name")
                .maxLength("locationDetail", it.getLocationDetail(), 120, "The place detail")
                .maxLength("description", it.getDescription(), 2000, "The description");
        Integer categoryId = v.id("categoryId", req.getParameter("categoryId"), "Pick the category that fits best.");
        Integer locationId = v.id("locationId", req.getParameter("locationId"), "Pick where it happened.");
        if (categoryId != null && categories.findById(categoryId) == null) {
            v.check(false, "categoryId", "That category no longer exists. Pick another one.");
        }
        if (locationId != null && locations.findById(locationId) == null) {
            v.check(false, "locationId", "That place no longer exists. Pick another one.");
        }
        it.setCategoryId(categoryId == null ? 0 : categoryId);
        it.setLocationId(locationId == null ? 0 : locationId);
        it.setEventDate(v.date("eventDate", req.getParameter("eventDate"), "date"));
        Integer heldAt = intParam(req, "heldAtId");
        if ("found".equals(it.getKind()) && heldAt != null) {
            var post = locations.findById(heldAt);
            v.check(post != null && post.isSkkPost(), "heldAtId", "Pick one of the SKK posts, or leave it empty if you still have the item.");
            it.setHeldAtId(heldAt);
        }
        int stored = editing ? (int) photos.countByItem(existing.getId()) : 0;
        List<Uploads.Photo> uploads = Uploads.read(req, "photos", stored, v);

        if (!v.ok()) {
            req.setAttribute("form", it);
            req.setAttribute("errors", v.errors());
            res.setStatus(422);
            showForm(req, res, editing);
            return;
        }

        int id = Tx.run(() -> {
            int itemId;
            if (editing) {
                itemService.update(it, me);
                itemId = it.getId();
            } else {
                itemId = itemService.create(it);
            }
            for (Uploads.Photo ph : uploads) {
                photos.add(itemId, ph.bytes, ph.mime);
            }
            return itemId;
        });
        if (editing) {
            Flash.ok(req, "Changes saved.");
        } else {
            Item created = items.findById(id);
            Flash.stamped(req, "Posted. Your tag " + created.getCode() + " is on the board.");
        }
        redirect(req, res, "/items/" + id);
    }

    private void fileClaim(HttpServletRequest req, HttpServletResponse res, User me, Item item) throws ServletException, IOException {
        String proof = Validator.clean(req.getParameter("proof"));
        Integer meet = intParam(req, "meetLocationId");
        Validator v = new Validator()
                .required("proof", proof, "Describe something only the owner would know, like a sticker or what is inside.")
                .minLength("proof", proof, 15, "Add a little more detail so the finder can be sure it is yours.")
                .maxLength("proof", proof, 1000, "The proof");
        String why = claimService.whyCannotClaim(item, me);
        if (why != null) {
            throw item.getReporterId() == me.getId()
                    ? id.ketemu.service.ServiceException.forbidden(why)
                    : id.ketemu.service.ServiceException.conflict(why);
        }
        if (!v.ok()) {
            req.setAttribute("claimErrors", v.errors());
            req.setAttribute("claimProof", proof);
            req.setAttribute("claimMeet", meet);
            res.setStatus(422);
            detail(req, res, item);
            return;
        }
        int claimId = claimService.file(item, me, proof, meet);
        Claim c = claims.findById(claimId);
        Flash.ok(req, "Claim filed. Your ticket is " + c.getNumber() + ".");
        redirect(req, res, "/claims/" + claimId);
    }
}
