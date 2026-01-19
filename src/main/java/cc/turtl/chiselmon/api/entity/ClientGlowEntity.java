package cc.turtl.chiselmon.api.entity;

public interface ClientGlowEntity {
    void chiselmon$setClientGlowColor(Integer color);

    void chiselmon$setClientGlowing(boolean glowing);
    
    Integer chiselmon$getClientGlowColor();
}