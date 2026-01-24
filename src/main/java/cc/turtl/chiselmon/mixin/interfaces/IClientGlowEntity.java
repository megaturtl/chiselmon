package cc.turtl.chiselmon.mixin.interfaces;

public interface IClientGlowEntity {
    void chiselmon$setClientGlowColor(Integer color);

    void chiselmon$setClientGlowing(boolean glowing);
    
    Integer chiselmon$getClientGlowColor();
}