public abstract class ObstacleModifer {
    /**
     * Enum to represent which phase of the match we are currently in.
     */
    public enum MatchPhase {
        /**
         * Represents endgame (for 2024, last 20 seconds)
         */
        ENDGAME,
        /**
         * Represents autonomous (first 15 seconds)
         */
        AUTO,
        /**
         * Represents teleop (between auto and tele, 1 min 55 seconds in 2024)
         */
        TELE
    }

    /**
     * All available obstacle modifiers
     */
    public enum ObstacleModifiers {
        /**
         * Obstacle is always active
         */
        ALWAYS_ACTIVE
    }

    /**
     * Whether or not the obstacle is active at this time.
     * @return true if the obstacle is currently active, false if not
     */
    public abstract boolean isActive();

    /**
     * Get the current phase of the match
     * @return The current phase of the match
     */
    protected MatchPhase getCurrentPhase(){
        return MatchPhase.ENDGAME;
    }
}