package cc.turtl.cobbleaid.feature.spawnalert;

public enum AlertPriority {
    NONE(0),
    CUSTOM(1),
    SIZE(2),
    SHINY(3),
    LEGENDARY(4);

    public final int weight;

    AlertPriority(int weight) {
        this.weight = weight;
    }
}
