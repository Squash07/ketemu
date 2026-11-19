package id.ketemu.web;

import id.ketemu.dao.CategoryDao;
import id.ketemu.model.Category;
import id.ketemu.util.Flash;
import id.ketemu.util.Icons;
import id.ketemu.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Desk: list, add, rename (and change the icon of) and delete categories. */
@WebServlet(urlPatterns = {"/admin/categories", "/admin/categories/*"})
public class AdminCategoryServlet extends BaseServlet {

    private final CategoryDao categories = new CategoryDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String[] p = parts(req);
        if (p.length == 0) {
            list(req, res, new Category());
            return;
        }
        Category c = found(categories.findById(idAt(p, 0)));
        if (p.length > 1 && "edit".equals(p[1])) {
            req.setAttribute("form", c);
            req.setAttribute("icons", Icons.ALL);
            render(req, res, "admin/category-edit");
            return;
        }
        throw HttpError.notFound();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String[] p = parts(req);
        if (p.length == 0) {
            Category form = read(req, 0);
            Validator v = validate(form);
            if (!v.ok()) {
                req.setAttribute("errors", v.errors());
                res.setStatus(422);
                list(req, res, form);
                return;
            }
            categories.create(form.getName(), form.getIcon());
            Flash.ok(req, "Category added.");
            redirect(req, res, "/admin/categories");
            return;
        }
        Category c = found(categories.findById(idAt(p, 0)));
        switch (p.length > 1 ? p[1] : "") {
            case "edit" -> {
                Category form = read(req, c.getId());
                Validator v = validate(form);
                if (!v.ok()) {
                    req.setAttribute("form", form);
                    req.setAttribute("icons", Icons.ALL);
                    renderForm(req, res, "admin/category-edit", v.errors());
                    return;
                }
                categories.update(c.getId(), form.getName(), form.getIcon());
                Flash.ok(req, "Category saved.");
                redirect(req, res, "/admin/categories");
            }
            case "delete" -> {
                if (c.getItemCount() > 0) {
                    Flash.error(req, c.getItemCount() + (c.getItemCount() == 1 ? " item uses " : " items use ") + c.getName()
                            + ". Move " + (c.getItemCount() == 1 ? "it" : "them") + " to another category first.");
                } else {
                    categories.delete(c.getId());
                    Flash.ok(req, "Category deleted.");
                }
                redirect(req, res, "/admin/categories");
            }
            default -> throw HttpError.notFound();
        }
    }

    private void list(HttpServletRequest req, HttpServletResponse res, Category form) throws ServletException, IOException {
        req.setAttribute("categories", categories.listAll());
        req.setAttribute("icons", Icons.ALL);
        req.setAttribute("form", form);
        render(req, res, "admin/categories");
    }

    private Category read(HttpServletRequest req, int id) {
        Category c = new Category();
        c.setId(id);
        c.setName(Validator.clean(req.getParameter("name")));
        c.setIcon(Validator.clean(req.getParameter("icon")));
        return c;
    }

    private Validator validate(Category c) {
        Validator v = new Validator()
                .required("name", c.getName(), "Give the category a name.")
                .maxLength("name", c.getName(), 40, "The name")
                .check(Icons.isValid(c.getIcon()), "icon", "Pick one of the icons.");
        if (v.ok() && categories.nameTaken(c.getName(), c.getId())) {
            v.check(false, "name", "There is already a category called " + c.getName() + ".");
        }
        return v;
    }
}
