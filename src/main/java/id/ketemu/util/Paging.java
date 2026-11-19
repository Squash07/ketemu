package id.ketemu.util;

/** Page numbers for lists. Pages start at 1. */
public class Paging {

    private final int page;
    private final int size;
    private final long total;

    public Paging(String pageParam, int size, long total) {
        int p = 1;
        try {
            p = Integer.parseInt(pageParam);
        } catch (NumberFormatException ignored) {
            p = 1;
        }
        this.size = size;
        this.total = total;
        int last = (int) Math.max(1, (total + size - 1) / size);
        this.page = Math.min(Math.max(1, p), last);
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotal() {
        return total;
    }

    public int getPages() {
        return (int) Math.max(1, (total + size - 1) / size);
    }

    public int getOffset() {
        return (page - 1) * size;
    }

    public boolean isHasPrev() {
        return page > 1;
    }

    public boolean isHasNext() {
        return page < getPages();
    }

    public int getPrev() {
        return page - 1;
    }

    public int getNext() {
        return page + 1;
    }

    public long getFrom() {
        return total == 0 ? 0 : getOffset() + 1;
    }

    public long getTo() {
        return Math.min(total, (long) getOffset() + size);
    }
}
