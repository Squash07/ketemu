package id.ketemu.util;

import java.util.List;

/** Category icon names, drawn as inline SVG in WEB-INF/tags/icon.tag. */
public final class Icons {

    public static final List<String> ALL = List.of(
            "wallet", "card", "key", "charger", "earbuds", "calc",
            "umbrella", "tumbler", "bag", "book", "shirt", "box");

    private Icons() {
    }

    public static boolean isValid(String icon) {
        return icon != null && ALL.contains(icon);
    }

    /** Picks a more specific icon from the item title when one fits. */
    public static String guess(String title, String categoryIcon) {
        String t = title == null ? "" : title.toLowerCase();
        if (t.contains("umbrella") || t.contains("payung")) return "umbrella";
        if (t.contains("tumbler") || t.contains("bottle") || t.contains("botol")) return "tumbler";
        if (t.contains("earbud") || t.contains("earphone") || t.contains("headphone") || t.contains("airpod")) return "earbuds";
        if (t.contains("calculator") || t.contains("casio") || t.contains("kalkulator")) return "calc";
        if (t.contains("charger") || t.contains("cable") || t.contains("kabel")) return "charger";
        if (t.contains("key") || t.contains("kunci")) return "key";
        if (t.contains("ktm") || t.contains("card") || t.contains("kartu") || t.contains("lanyard")) return "card";
        if (t.contains("wallet") || t.contains("dompet")) return "wallet";
        if (t.contains("backpack") || t.contains("bag") || t.contains("tas")) return "bag";
        return isValid(categoryIcon) ? categoryIcon : "box";
    }
}
