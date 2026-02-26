package cc.turtl.chiselmon.feature.pc;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Per world PC User Data.
 */
public class PCUserData {
    public final Bookmarks bookmarks = new Bookmarks();

    public static class Bookmarks {
        public static final int MAX_BOOKMARKS = 5;
        private final Set<Integer> data = new LinkedHashSet<>();

        public boolean toggle(int boxNo) {
            return data.remove(boxNo) || (data.size() < MAX_BOOKMARKS && data.add(boxNo));
        }

        public void remove(int boxNo) {
            data.remove(boxNo);
        }

        public void clear() {
            data.clear();
        }

        public boolean isFull() {
            return data.size() >= MAX_BOOKMARKS;
        }

        public boolean has(int boxNo) {
            return data.contains(boxNo);
        }

        public List<Integer> get() {
            return new ArrayList<>(data);
        }
    }
}