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
    public enum ObstacleModifierTypes {
        /**
         * Obstacle is always active
         */
        ALWAYS_ACTIVE,
        /**
         * Obstacle is active during endgame (last 20 seconds)
         */
        ACTIVE_ENGAME,
        /**
         * Obstacle is active during autonomous (first 15 seconds)
         */
        ACTIVE_AUTO,
        /**
         * Obstacle is active during teleop
         */
        ACTIVE_TELE,
        /**
         * Obstacle is active if it is my alliance's
         */
        ACTIVE_MY_ALLIANCE,
        /**
         * Obstacle is active if it is the other alliance's
         */
        ACTIVE_OTHER_ALLIANCE,
        /**
         * Marks the obstacle as the blue alliance's, does not affect if it is active
         * unless ACTIVE_MY_ALLIANCE/ACTIVE_OTHER_ALLIANCE is also applied
         */
        BLUE_ALLIANCE,
        /**
         * Marks the obstacle as the red alliance's, does not affect if it is active
         * unless ACTIVE_MY_ALLIANCE/ACTIVE_OTHER_ALLIANCE is also applied
         */
        RED_ALLIANCE
    }

    public static ObstacleModifer getModifier(ObstacleModifierTypes type) {
        switch (type) {
            case ALWAYS_ACTIVE:
                break;
            case ACTIVE_ENGAME:
                break;
            case ACTIVE_AUTO:
                break;
            case ACTIVE_TELE:
                break;
            case ACTIVE_MY_ALLIANCE:
                break;
            case ACTIVE_OTHER_ALLIANCE:
                break;
            case BLUE_ALLIANCE:
                break;
            case RED_ALLIANCE:
                break;
        }
        throw new NotImplementedException("TODO");
    }

    /**
     * Whether or not the obstacle is active at this time.
     * 
     * @return true if the obstacle is currently active, false if not
     */
    public abstract boolean isActive();

    /**
     * Whether or not this modifier MUST be active to have the obstacle active.
     * If true, will use the AND operator with other modifiers, if false, uses OR.
     * @return True if required to keep the obstacle active, false if not required.
     */
    public abstract boolean requiredForActive();

    /**
     * Get the current phase of the match
     * 
     * @return The current phase of the match
     */
    protected MatchPhase getCurrentPhase() {
        throw new NotImplementedException("TODO");
    }
}