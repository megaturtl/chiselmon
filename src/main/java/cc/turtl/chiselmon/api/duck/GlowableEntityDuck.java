package cc.turtl.chiselmon.api.duck;

public interface GlowableEntityDuck {
    void chiselmon$setClientGlowColor(Integer color);

    void chiselmon$setClientGlowing(boolean glowing);
    
    Integer chiselmon$getClientGlowColor();
}