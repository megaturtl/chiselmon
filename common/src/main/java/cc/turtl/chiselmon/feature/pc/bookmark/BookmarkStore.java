package cc.turtl.chiselmon.feature.pc.bookmark;

import java.util.*;

/**
 * Stores bookmark data safely to avoid going over the max
 */
public class BookmarkStore {
    public static final int MAX_BOOKMARKS = 5;
    private final Set<Integer> bookmarkedBoxes;

    public BookmarkStore() {
        bookmarkedBoxes = new LinkedHashSet<>();
    }

    public void addIfSpace(int boxNo) {
        if (!isFull()) {
            bookmarkedBoxes.add(boxNo);
        }
    }

    public void toggle(int boxNo) {
        if (has(boxNo)) {
            remove(boxNo);
        } else {
            addIfSpace(boxNo);
        }
    }

    public void remove(int boxNo) {
        bookmarkedBoxes.remove(boxNo);
    }

    public void clear() {
        bookmarkedBoxes.clear();
    }

    public boolean isFull() {
        return (bookmarkedBoxes.size() >= MAX_BOOKMARKS);
    }

    public boolean has(int boxNo) {
        return bookmarkedBoxes.contains(boxNo);
    }

    public List<Integer> getList() {
        return new ArrayList<>(bookmarkedBoxes);
    }
}