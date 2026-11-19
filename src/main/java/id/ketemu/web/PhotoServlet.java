package id.ketemu.web;

import id.ketemu.dao.PhotoDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Streams a photo from the database with caching headers. */
@WebServlet(urlPatterns = {"/photo/*"})
public class PhotoServlet extends BaseServlet {

    private final PhotoDao photos = new PhotoDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        int id = idAt(parts(req), 0);
        PhotoDao.Blob b = photos.load(id);
        if (b == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String etag = "\"p" + b.id + "-" + b.version + "\"";
        res.setHeader("ETag", etag);
        res.setHeader("Cache-Control", "public, max-age=86400");
        if (etag.equals(req.getHeader("If-None-Match"))) {
            res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        res.setCharacterEncoding((String) null);
        res.setContentType(b.mimeType);
        res.setContentLength(b.content.length);
        res.getOutputStream().write(b.content);
    }
}
