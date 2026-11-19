package id.ketemu.web;

import id.ketemu.dao.LocationDao;
import id.ketemu.model.Location;
import id.ketemu.util.Flash;
import id.ketemu.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Desk: list, add, edit and delete campus locations, including which ones are SKK posts. */
@WebServlet(urlPatterns = {"/admin/locations", "/admin/locations/*"})
public class AdminLocationServlet extends BaseServlet {

    private final LocationDao locations = new LocationDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String[] p = parts(req);
        if (p.length == 0) {
            list(req, res, new Location());
            return;
        }
        Location l = found(locations.findById(idAt(p, 0)));
        if (p.length > 1 && "edit".equals(p[1])) {
            req.setAttribute("form", l);
            render(req, res, "admin/location-edit");
            return;
        }
        throw HttpError.notFound();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String[] p = parts(req);
        if (p.length == 0) {
            Location form = read(req, 0);
            Validator v = validate(form);
            if (!v.ok()) {
                req.setAttribute("errors", v.errors());
                res.setStatus(422);
                list(req, res, form);
                return;
            }
            locations.create(form.getName(), form.getZone(), form.isSkkPost());
            Flash.ok(req, "Location added.");
            redirect(req, res, "/admin/locations");
            return;
        }
        Location l = found(locations.findById(idAt(p, 0)));
        switch (p.length > 1 ? p[1] : "") {
            case "edit" -> {
                Location form = read(req, l.getId());
                Validator v = validate(form);
                if (!v.ok()) {
                    req.setAttribute("form", form);
                    renderForm(req, res, "admin/location-edit", v.errors());
                    return;
                }
                locations.update(l.getId(), form.getName(), form.getZone(), form.isSkkPost());
                Flash.ok(req, "Location saved.");
                redirect(req, res, "/admin/locations");
            }
            case "delete" -> {
                if (l.getItemCount() > 0) {
                    Flash.error(req, l.getItemCount() + (l.getItemCount() == 1 ? " item was" : " items were") + " reported at " + l.getName()
                            + ". Move " + (l.getItemCount() == 1 ? "it" : "them") + " to another location first.");
                } else {
                    locations.delete(l.getId());
                    long held = locations.countHeldItems(l.getId());
                    Flash.ok(req, "Location deleted." + (held > 0 ? " Items it was holding now show no post." : ""));
                }
                redirect(req, res, "/admin/locations");
            }
            default -> throw HttpError.notFound();
        }
    }

    private void list(HttpServletRequest req, HttpServletResponse res, Location form) throws ServletException, IOException {
        req.setAttribute("locations", locations.listAll());
        req.setAttribute("form", form);
        render(req, res, "admin/locations");
    }

    private Location read(HttpServletRequest req, int id) {
        Location l = new Location();
        l.setId(id);
        l.setName(Validator.clean(req.getParameter("name")));
        l.setZone(Validator.orNull(req.getParameter("zone")));
        l.setSkkPost(req.getParameter("skkPost") != null);
        return l;
    }

    private Validator validate(Location l) {
        Validator v = new Validator()
                .required("name", l.getName(), "Give the location a name, like Perpustakaan Pusat.")
                .maxLength("name", l.getName(), 80, "The name")
                .maxLength("zone", l.getZone(), 40, "The zone");
        if (v.ok() && locations.nameTaken(l.getName(), l.getId())) {
            v.check(false, "name", "There is already a location called " + l.getName() + ".");
        }
        return v;
    }
}
