package id.ketemu.service;

import id.ketemu.dao.NotificationDao;
import id.ketemu.dao.WatchlistDao;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class NotificationService {

    private final NotificationDao notifications = new NotificationDao();
    private final WatchlistDao watchlist = new WatchlistDao();

    public void notify(int userId, Integer itemId, String message) {
        notifications.create(userId, itemId, message);
    }

    /** Tells everyone who saved the item, except the people in {@code skip}. */
    public void notifyWatchers(int itemId, String message, Integer... skip) {
        Set<Integer> skipped = new HashSet<>(Arrays.asList(skip));
        for (int userId : watchlist.watchersOf(itemId)) {
            if (!skipped.contains(userId)) {
                notifications.create(userId, itemId, message);
            }
        }
    }
}
