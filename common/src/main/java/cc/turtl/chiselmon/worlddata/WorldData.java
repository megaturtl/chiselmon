package cc.turtl.chiselmon.worlddata;

import cc.turtl.chiselmon.feature.pc.bookmark.BookmarkStore;
import com.google.gson.annotations.SerializedName;

/**
 * Data stored per world/server. Serialised as JSON in the mod config directory
 */
public class WorldData {

    @SerializedName("bookmarks")
    public BookmarkStore bookmarkStore = new BookmarkStore();
}