package cc.turtl.chiselmon.api;

public enum Priority {
    LOWEST, LOW, NORMAL, HIGH, HIGHEST;

    public boolean isHigherThan(Priority other) {
        return this.ordinal() > other.ordinal();
    }
}