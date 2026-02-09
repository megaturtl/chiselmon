package cc.turtl.chiselmon.api;

public enum Priority {
    HIGHEST, HIGH, NORMAL, LOW, LOWEST;

    public boolean isHigherThan(Priority other) {
        return this.ordinal() > other.ordinal();
    }
}