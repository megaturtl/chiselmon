package cc.turtl.chiselmon.api.duck;

public interface DuckGlowableEntity {
    void chiselmon$setClientGlowColor(Integer rgb);

    void chiselmon$setClientGlowing(boolean glowing);

    Integer chiselmon$getClientGlowColor();
}