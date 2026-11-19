package id.ketemu.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Date formatting used by the views (EL cannot format java.time values directly). */
public final class Fmt {

    private static final DateTimeFormatter DAY_MONTH = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter LONG = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ENGLISH);

    private Fmt() {
    }

    public static String dayMonth(LocalDate d) {
        return d == null ? "" : d.format(DAY_MONTH);
    }

    public static String date(LocalDate d) {
        return d == null ? "" : d.format(DATE);
    }

    public static String longDate(LocalDate d) {
        return d == null ? "" : d.format(LONG);
    }

    public static String ago(LocalDateTime t) {
        if (t == null) {
            return "";
        }
        Duration d = Duration.between(t, LocalDateTime.now());
        long min = d.toMinutes();
        if (min < 1) {
            return "just now";
        }
        if (min < 60) {
            return min + (min == 1 ? " minute ago" : " minutes ago");
        }
        long h = d.toHours();
        if (h < 24) {
            return h + (h == 1 ? " hour ago" : " hours ago");
        }
        long days = d.toDays();
        if (days == 1) {
            return "yesterday";
        }
        if (days < 7) {
            return days + " days ago";
        }
        return t.toLocalDate().format(t.getYear() == LocalDate.now().getYear() ? DAY_MONTH : DATE);
    }
}
