package cc.turtl.chiselmon.api.calc.capture;

@FunctionalInterface
public interface BallStrategy {

    /**
     * Calculates the catch rate multiplier for this ball type.
     *
     * @param context The capture context containing all relevant information
     * @return The catch rate multiplier (typically 1.0 - 5.0, or 999.0 for guaranteed capture)
     */
    float calculate(CaptureContext context);
}