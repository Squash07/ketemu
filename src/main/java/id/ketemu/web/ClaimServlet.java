package id.ketemu.web;

import id.ketemu.dao.ClaimDao;
import id.ketemu.dao.ItemDao;
import id.ketemu.dao.LocationDao;
import id.ketemu.model.Claim;
import id.ketemu.model.User;
import id.ketemu.service.ClaimService;
import id.ketemu.service.ServiceException;
import id.ketemu.util.Flash;
import id.ketemu.util.Validator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** My claims, the claim ticket, editing proof, and the finder's decisions. */
@WebServlet(urlPatterns = {"/claims", "/claims/*"})
public class ClaimServlet extends BaseServlet {

    private final ClaimDao claims = new ClaimDao();
    private final ItemDao items = new ItemDao();
    private final LocationDao locations = new LocationDao();
    private final ClaimService service = new ClaimService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User me = requireUser(req);
        String[] p = parts(req);
        if (p.length == 0) {
            req.setAttribute("claims", claims.listByClaimant(me.getId()));
            render(req, res, "claims/list");
            return;
        }
        Claim claim = load(p, me);
        String action = p.length > 1 ? p[1] : "";
        switch (action) {
            case "" -> {
                req.setAttribute("claim", claim);
                req.setAttribute("item", items.findById(claim.getItemId()));
                req.setAttribute("isClaimant", claim.getClaimantId() == me.getId());
                req.setAttribute("isFinder", claim.getFinderId() == me.getId());
                render(req, res, "claims/ticket");
            }
            case "edit" -> {
                if (claim.getClaimantId() != me.getId()) {
                    throw ServiceException.forbidden("Only the person who filed this claim can edit it.");
                }
                if (!claim.isPending()) {
                    Flash.error(req, "This claim has been decided, so the proof can no longer be changed.");
                    redirect(req, res, "/claims/" + claim.getId());
                    return;
                }
                req.setAttribute("claim", claim);
                req.setAttribute("proof", claim.getProof());
                req.setAttribute("meet", claim.getMeetLocationId());
                req.setAttribute("posts", locations.listPosts());
                render(req, res, "claims/edit");
            }
            default -> throw HttpError.notFound();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        User me = requireUser(req);
        String[] p = parts(req);
        Claim claim = load(p, me);
        String action = p.length > 1 ? p[1] : "";
        switch (action) {
            case "edit" -> {
                String proof = Validator.clean(req.getParameter("proof"));
                Integer meet = intParam(req, "meetLocationId");
                Validator v = new Validator()
                        .required("proof", proof, "Describe something only the owner would know.")
                        .minLength("proof", proof, 15, "Add a little more detail so the finder can be sure it is yours.")
                        .maxLength("proof", proof, 1000, "The proof");
                if (!v.ok()) {
                    req.setAttribute("claim", claim);
                    req.setAttribute("proof", proof);
                    req.setAttribute("meet", meet);
                    req.setAttribute("posts", locations.listPosts());
                    renderForm(req, res, "claims/edit", v.errors());
                    return;
                }
                service.editProof(claim, me, proof, meet);
                Flash.ok(req, "Claim updated.");
                redirect(req, res, "/claims/" + claim.getId());
            }
            case "approve" -> {
                service.approve(claim, me);
                Flash.ok(req, "Claim approved. " + claim.getClaimantName() + " can now see where to meet you.");
                redirect(req, res, "/items/" + claim.getItemId() + "#claim");
            }
            case "reject" -> {
                service.reject(claim, me);
                Flash.ok(req, "Claim rejected.");
                redirect(req, res, "/items/" + claim.getItemId() + "#claim");
            }
            case "cancel" -> {
                service.cancel(claim, me);
                Flash.ok(req, "Claim cancelled.");
                redirect(req, res, "/claims");
            }
            default -> throw HttpError.notFound();
        }
    }

    private Claim load(String[] p, User me) {
        Claim claim = found(claims.findById(idAt(p, 0)));
        if (!service.canView(claim, me)) {
            throw ServiceException.forbidden("This claim ticket belongs to someone else.");
        }
        return claim;
    }
}
