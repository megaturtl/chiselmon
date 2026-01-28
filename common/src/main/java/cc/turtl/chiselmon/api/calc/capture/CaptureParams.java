package cc.turtl.chiselmon.api.calc.capture;

/**
 * Immutable parameters for capture rate calculation.
 * Use the builder to construct instances.
 */
public record CaptureParams(
        float maxHp,
        float currentHp,
        float catchRate,
        int targetLevel,
        float statusMultiplier,
        float inBattleModifier,
        float darkGrassModifier,
        float levelBonus,
        float ballBonus,
        float pokedexMultiplier
) {

    public CaptureParams {
        // Validation
        if (maxHp <= 0) throw new IllegalArgumentException("maxHp must be positive");
        if (currentHp < 0) throw new IllegalArgumentException("currentHp cannot be negative");
        if (currentHp > maxHp) throw new IllegalArgumentException("currentHp cannot exceed maxHp");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private float maxHp;
        private float currentHp;
        private float catchRate;
        private int targetLevel;
        private float statusMultiplier = 1.0F;
        private float inBattleModifier = 1.0F;
        private float darkGrassModifier = 1.0F;
        private float levelBonus = 1.0F;
        private float ballBonus = 1.0F;
        private float pokedexMultiplier = 1.0F;

        public Builder maxHp(float maxHp) {
            this.maxHp = maxHp;
            return this;
        }

        public Builder currentHp(float currentHp) {
            this.currentHp = currentHp;
            return this;
        }

        public Builder catchRate(float catchRate) {
            this.catchRate = catchRate;
            return this;
        }

        public Builder targetLevel(int targetLevel) {
            this.targetLevel = targetLevel;
            return this;
        }

        public Builder statusMultiplier(float statusMultiplier) {
            this.statusMultiplier = statusMultiplier;
            return this;
        }

        public Builder inBattleModifier(float inBattleModifier) {
            this.inBattleModifier = inBattleModifier;
            return this;
        }

        public Builder darkGrassModifier(float darkGrassModifier) {
            this.darkGrassModifier = darkGrassModifier;
            return this;
        }

        public Builder levelBonus(float levelBonus) {
            this.levelBonus = levelBonus;
            return this;
        }

        public Builder ballBonus(float ballBonus) {
            this.ballBonus = ballBonus;
            return this;
        }

        public Builder pokedexMultiplier(float pokedexMultiplier) {
            this.pokedexMultiplier = pokedexMultiplier;
            return this;
        }

        public CaptureParams build() {
            return new CaptureParams(
                    maxHp, currentHp, catchRate, targetLevel,
                    statusMultiplier, inBattleModifier, darkGrassModifier,
                    levelBonus, ballBonus, pokedexMultiplier
            );
        }
    }
}