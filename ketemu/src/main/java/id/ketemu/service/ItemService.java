package id.ketemu.service;

import id.ketemu.dao.ItemDao;
import id.ketemu.dao.Tx;
import id.ketemu.model.Item;
import id.ketemu.model.User;

/** Ownership and status rules for item reports. */
public class ItemService {

    private final ItemDao items = new ItemDao();
    private final NotificationService notes = new NotificationService();

    public static boolean isReporter(Item item, User user) {
        return user != null && item != null && item.getReporterId() == user.getId();
    }

    public void requireReporter(Item item, User user) {
        if (!isReporter(item, user)) {
            throw ServiceException.forbidden("Only the person who posted this item can change it.");
        }
    }

    public void requireReporterOrAdmin(Item item, User user) {
        if (!isReporter(item, user) && (user == null || !user.isAdmin())) {
            throw ServiceException.forbidden("Only the person who posted this item, or the SKK desk, can remove it.");
        }
    }

    public int create(Item item) {
        return items.create(item);
    }

    public void update(Item item, User user) {
        Item current = items.findById(item.getId());
        requireReporter(current, user);
        items.update(item);
        notes.notifyWatchers(item.getId(), "The details of " + current.getCode() + " were updated.", user.getId());
    }

    public void delete(Item item, User user) {
        requireReporterOrAdmin(item, user);
        items.delete(item.getId());
    }

    /** The owner of a lost item says they have it back. */
    public void markRecovered(Item item, User user) {
        requireReporter(item, user);
        if (!item.isLost() || !item.isOpen()) {
            throw ServiceException.conflict("Only an open lost report can be marked as back with you.");
        }
        Tx.run(() -> {
            items.updateStatus(item.getId(), "returned");
            notes.notifyWatchers(item.getId(), item.getCode() + " " + item.getTitle() + " is back with its owner.", user.getId());
        });
    }

    public void close(Item item, User user) {
        requireReporter(item, user);
        if (!item.isOpen()) {
            throw ServiceException.conflict("Only an open report can be closed.");
        }
        Tx.run(() -> {
            items.updateStatus(item.getId(), "closed");
            notes.notifyWatchers(item.getId(), item.getCode() + " was closed by the person who posted it.", user.getId());
        });
    }

    public void reopen(Item item, User user) {
        requireReporter(item, user);
        if (!"closed".equals(item.getStatus())) {
            throw ServiceException.conflict("Only a closed report can be reopened.");
        }
        Tx.run(() -> {
            items.updateStatus(item.getId(), "open");
            notes.notifyWatchers(item.getId(), item.getCode() + " is back on the board.", user.getId());
        });
    }
}
