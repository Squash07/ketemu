package id.ketemu.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/** Collects field errors for a form. Messages say what is wrong and how to fix it. */
public class Validator {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern NRP = Pattern.compile("^[0-9]{10}$|^[A-Za-z0-9]{4,12}$");
    private static final Pattern PHONE = Pattern.compile("^[0-9+()\\-\\s]{6,20}$");

    private final Map<String, String> errors = new LinkedHashMap<>();

    public static String clean(String s) {
        return s == null ? "" : s.trim();
    }

    public static String orNull(String s) {
        String c = clean(s);
        return c.isEmpty() ? null : c;
    }

    public Validator required(String field, String value, String message) {
        if (!errors.containsKey(field) && clean(value).isEmpty()) {
            errors.put(field, message);
        }
        return this;
    }

    public Validator maxLength(String field, String value, int max, String label) {
        if (!errors.containsKey(field) && clean(value).length() > max) {
            errors.put(field, label + " is too long. Keep it under " + max + " characters.");
        }
        return this;
    }

    public Validator minLength(String field, String value, int min, String message) {
        if (!errors.containsKey(field) && !clean(value).isEmpty() && clean(value).length() < min) {
            errors.put(field, message);
        }
        return this;
    }

    public Validator email(String field, String value) {
        if (!errors.containsKey(field) && !clean(value).isEmpty() && !EMAIL.matcher(clean(value)).matches()) {
            errors.put(field, "That email address is missing something. Use the form name@example.com.");
        }
        return this;
    }

    public Validator nrp(String field, String value) {
        if (!errors.containsKey(field) && !clean(value).isEmpty() && !NRP.matcher(clean(value)).matches()) {
            errors.put(field, "An NRP is 10 digits, for example 5025231117.");
        }
        return this;
    }

    public Validator phone(String field, String value) {
        if (!errors.containsKey(field) && !clean(value).isEmpty() && !PHONE.matcher(clean(value)).matches()) {
            errors.put(field, "Use digits only, for example 081234567890.");
        }
        return this;
    }

    public LocalDate date(String field, String value, String label) {
        if (errors.containsKey(field)) {
            return null;
        }
        String v = clean(value);
        if (v.isEmpty()) {
            errors.put(field, "Add the " + label + ".");
            return null;
        }
        try {
            LocalDate d = LocalDate.parse(v);
            if (d.isAfter(LocalDate.now())) {
                errors.put(field, "That date is in the future. Use the day it happened.");
                return null;
            }
            if (d.isBefore(LocalDate.now().minusYears(1))) {
                errors.put(field, "Ketemu keeps reports from the last 12 months. Pick a more recent date.");
                return null;
            }
            return d;
        } catch (DateTimeParseException e) {
            errors.put(field, "Use a date like 18/11/2026.");
            return null;
        }
    }

    public Integer id(String field, String value, String message) {
        if (errors.containsKey(field)) {
            return null;
        }
        try {
            int v = Integer.parseInt(clean(value));
            if (v > 0) {
                return v;
            }
        } catch (NumberFormatException ignored) {
            // falls through to the error below
        }
        errors.put(field, message);
        return null;
    }

    public Validator check(boolean ok, String field, String message) {
        if (!ok && !errors.containsKey(field)) {
            errors.put(field, message);
        }
        return this;
    }

    public boolean ok() {
        return errors.isEmpty();
    }

    public Map<String, String> errors() {
        return errors;
    }
}
