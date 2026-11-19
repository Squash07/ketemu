package id.ketemu.web;

import id.ketemu.dao.ItemDao;
import id.ketemu.dao.PhotoDao;
import id.ketemu.model.Item;
import id.ketemu.model.ItemPhoto;
import id.ketemu.model.User;
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

/** Replace a photo, make it the cover, or remove it. Only the reporter of the item may do this. */
@WebServlet(urlPatterns = {"/photos/*"})
@MultipartConfig(maxFileSize = 1_048_576, maxRequestSize = 2 * 1_048_576)
public class PhotoManageServlet extends BaseServlet {

    private final PhotoDao photos = new PhotoDao();
    private final ItemDao items = new ItemDao();
    private final ItemService itemService = new ItemService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User me = requireUser(req);
        String[] p = parts(req);
        ItemPhoto photo = found(photos.findMeta(idAt(p, 0)));
        Item item = found(items.findById(photo.getItemId()));
        itemService.requireReporter(item, me);
        String back = "/items/" + item.getId() + "/edit#photos";
        switch (p.length > 1 ? p[1] : "") {
            case "cover" -> {
                photos.makeCover(item.getId(), photo.getId());
                Flash.ok(req, "Cover photo changed.");
            }
            case "delete" -> {
                photos.delete(photo.getId());
                Flash.ok(req, "Photo removed.");
            }
            case "replace" -> {
                Validator v = new Validator();
                List<Uploads.Photo> up = Uploads.read(req, "photo", 0, v);
                if (!v.ok()) {
                    Flash.error(req, v.errors().values().iterator().next());
                } else if (up.isEmpty()) {
                    Flash.error(req, "Choose a photo to put in its place.");
                } else {
                    photos.replace(photo.getId(), up.get(0).bytes, up.get(0).mime);
                    Flash.ok(req, "Photo replaced.");
                }
            }
            default -> throw HttpError.notFound();
        }
        redirect(req, res, back);
    }
}
