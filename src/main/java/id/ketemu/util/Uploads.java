package id.ketemu.util;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/** Reads and checks uploaded photos. Photos are kept in memory and stored in the database, never on disk. */
public final class Uploads {

    public static final int MAX_BYTES = 1_048_576;
    public static final int MAX_PER_ITEM = 3;

    private Uploads() {
    }

    public static class Photo {
        public final byte[] bytes;
        public final String mime;

        Photo(byte[] bytes, String mime) {
            this.bytes = bytes;
            this.mime = mime;
        }
    }

    public static boolean isMultipart(HttpServletRequest req) {
        String ct = req.getContentType();
        return ct != null && ct.toLowerCase().startsWith("multipart/form-data");
    }

    /**
     * Returns the photos sent in the given field. Problems are added to the validator
     * under the same field name, written so the person knows what to change.
     */
    public static List<Photo> read(HttpServletRequest req, String field, int alreadyStored, Validator v)
            throws IOException, ServletException {
        List<Photo> out = new ArrayList<>();
        if (!isMultipart(req)) {
            return out;
        }
        for (Part part : req.getParts()) {
            if (!field.equals(part.getName()) || part.getSize() == 0) {
                continue;
            }
            String name = part.getSubmittedFileName() == null ? "That file" : part.getSubmittedFileName();
            if (part.getSize() > MAX_BYTES) {
                v.check(false, field, name + " is larger than 1 MB. Resize it and try again.");
                continue;
            }
            byte[] bytes;
            try (InputStream in = part.getInputStream()) {
                bytes = in.readAllBytes();
            }
            String mime = sniff(bytes);
            if (mime == null) {
                v.check(false, field, name + " is not a JPG, PNG or WebP image.");
                continue;
            }
            out.add(new Photo(bytes, mime));
        }
        if (alreadyStored + out.size() > MAX_PER_ITEM) {
            int room = Math.max(0, MAX_PER_ITEM - alreadyStored);
            v.check(false, field, room == 0
                    ? "This item already has 3 photos. Remove one before adding another."
                    : "An item can have 3 photos. You can add " + room + " more.");
        }
        return out;
    }

    /** Checks the file signature instead of trusting the browser's content type. */
    static String sniff(byte[] b) {
        if (b.length > 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (b.length > 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return "image/png";
        }
        if (b.length > 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "image/webp";
        }
        return null;
    }
}
