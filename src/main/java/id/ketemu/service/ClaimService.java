package id.ketemu.service;

import id.ketemu.dao.ClaimDao;
import id.ketemu.dao.ItemDao;
import id.ketemu.dao.LocationDao;
import id.ketemu.dao.Tx;
import id.ketemu.model.Claim;
import id.ketemu.model.Item;
import id.ketemu.model.Location;
import id.ketemu.model.User;

/** The claim and handover flow: file, edit, approve, reject, cancel, mark returned. */
public class ClaimService {

    private final ClaimDao claims = new ClaimDao();
    private final ItemDao items = new ItemDao();
    private final LocationDao locations = new LocationDao();
    private final NotificationService notes = new NotificationService();

    /** Returns null when the user may claim the item, or the reason they cannot. */
    public String whyCannotClaim(Item item, User user) {
        if (!item.isFound()) {
            return "Only found items can be claimed. If this is yours, leave a comment for the person who lost it.";
        }
        if (!item.isOpen()) {
            return "This item is no longer open for claims.";
        }
        if (user != null && item.getReporterId() == user.getId()) {
            return "You posted this item, so you cannot claim it.";
        }
        if (user != null && claims.activeFor(item.getId(), user.getId()) != null) {
            return "You already have a claim on this item.";
        }
        return null;
    }

    public void checkPickupPost(Integer locationId) {
        if (locationId == null) {
            return;
        }
        Location l = locations.findById(locationId);
        if (l == null || !l.isSkkPost()) {
            throw new ServiceException(400, "Pick one of the SKK posts for the pickup.");
        }
    }

    public int file(Item item, User user, String proof, Integer meetId) {
        String reason = whyCannotClaim(item, user);
        if (reason != null) {
            throw item.getReporterId() == user.getId() ? ServiceException.forbidden(reason) : ServiceException.conflict(reason);
        }
        checkPickupPost(meetId);
        return Tx.run(() -> {
            int id = claims.create(item.getId(), user.getId(), proof, meetId);
            notes.notify(item.getReporterId(), item.getId(),
                    user.getFullName() + " filed a claim on your item " + item.getCode() + ".");
            return id;
        });
    }

    public boolean canView(Claim claim, User user) {
        return user != null && (claim.getClaimantId() == user.getId() || claim.getFinderId() == user.getId() || user.isAdmin());
    }

    public void editProof(Claim claim, User user, String proof, Integer meetId) {
        if (claim.getClaimantId() != user.getId()) {
            throw ServiceException.forbidden("Only the person who filed this claim can edit it.");
        }
        if (!claim.isPending()) {
            throw ServiceException.conflict("This claim has been decided, so the proof can no longer be changed.");
        }
        checkPickupPost(meetId);
        claims.updateProof(claim.getId(), proof, meetId);
    }

    private void requireFinder(Claim claim, User user) {
        if (claim.getFinderId() != user.getId()) {
            throw ServiceException.forbidden("Only the person who found this item can decide on claims.");
        }
    }

    public void approve(Claim claim, User user) {
        requireFinder(claim, user);
        if (!claim.isPending() || !"open".equals(claim.getItemStatus())) {
            throw ServiceException.conflict("This claim can no longer be approved.");
        }
        Tx.run(() -> {
            claims.setStatus(claim.getId(), "approved");
            items.updateStatus(claim.getItemId(), "claimed");
            String post = claim.getMeetLocationName() != null ? claim.getMeetLocationName()
                    : (claim.getHeldAtName() != null ? claim.getHeldAtName() : "the SKK post you agree on");
            notes.notify(claim.getClaimantId(), claim.getItemId(),
                    "Your claim " + claim.getNumber() + " was approved. Pick it up at " + post + ".");
            for (int other : claims.rejectOthers(claim.getItemId(), claim.getId())) {
                notes.notify(other, claim.getItemId(),
                        "Your claim on " + claim.getItemCode() + " was not approved. The finder matched it to someone else.");
            }
            notes.notifyWatchers(claim.getItemId(), claim.getItemCode() + " " + claim.getItemTitle() + " has been claimed.",
                    claim.getClaimantId(), user.getId());
        });
    }

    public void reject(Claim claim, User user) {
        requireFinder(claim, user);
        if (!claim.isPending()) {
            throw ServiceException.conflict("Only a pending claim can be rejected.");
        }
        Tx.run(() -> {
            claims.setStatus(claim.getId(), "rejected");
            notes.notify(claim.getClaimantId(), claim.getItemId(),
                    "Your claim " + claim.getNumber() + " was not approved. You can comment on the item to add more detail.");
        });
    }

    public void cancel(Claim claim, User user) {
        if (claim.getClaimantId() != user.getId()) {
            throw ServiceException.forbidden("Only the person who filed this claim can cancel it.");
        }
        if (!claim.isActive() || "returned".equals(claim.getItemStatus())) {
            throw ServiceException.conflict("This claim can no longer be cancelled.");
        }
        Tx.run(() -> {
            claims.setStatus(claim.getId(), "cancelled");
            if (claim.isApproved() && "claimed".equals(claim.getItemStatus())) {
                items.updateStatus(claim.getItemId(), "open");
            }
            notes.notify(claim.getFinderId(), claim.getItemId(),
                    claim.getClaimantName() + " cancelled claim " + claim.getNumber() + ".");
        });
    }

    public void markReturned(Item item, User user) {
        if (item.getReporterId() != user.getId()) {
            throw ServiceException.forbidden("Only the person who found this item can mark it returned.");
        }
        if (!"claimed".equals(item.getStatus())) {
            throw ServiceException.conflict("Approve a claim before marking the item returned.");
        }
        Claim approved = claims.approvedFor(item.getId());
        Tx.run(() -> {
            items.updateStatus(item.getId(), "returned");
            if (approved != null) {
                notes.notify(approved.getClaimantId(), item.getId(),
                        item.getCode() + " " + item.getTitle() + " was marked returned. Glad it found its way back.");
            }
            notes.notifyWatchers(item.getId(), item.getCode() + " " + item.getTitle() + " was returned to its owner.",
                    user.getId(), approved == null ? null : approved.getClaimantId());
        });
    }
}
