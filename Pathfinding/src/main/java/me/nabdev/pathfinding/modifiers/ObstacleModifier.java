package me.nabdev.pathfinding.modifiers;

import java.lang.UnsupportedOperationException;

/**
 * Represents a modifier that changes when obstacles are active
 */
public abstract class ObstacleModifier {
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
        ACTIVE_ENDGAME,
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
        RED_ALLIANCE,
        /**
         * Marks the obstacle as a zone. This means it will not invalidate obstacle
         * points and start points will not snap outside of it.
         */
        ZONE_MODIFIER
    }

    /**
     * Get a modifier instance associated with the given type
     * 
     * @param type       The type of modifier to get
     * @param collection The collection of modifiers to associate with the modifier
     * @return The modifier associated with the given type
     */
    public static ObstacleModifier getModifier(ObstacleModifierTypes type, ModifierCollection collection) {
        switch (type) {
            case ALWAYS_ACTIVE:
                return new AlwaysActiveModifier();
            case ACTIVE_ENDGAME:
                return new ActiveEndgameModifier();
            case ACTIVE_AUTO:
                return new ActiveAutoModifier();
            case ACTIVE_TELE:
                return new ActiveTeleModifier();
            case ACTIVE_MY_ALLIANCE:
                return new ActiveMyAllianceModifier(collection);
            case ACTIVE_OTHER_ALLIANCE:
                return new ActiveOtherAllianceModifier(collection);
            case BLUE_ALLIANCE:
                return new BlueAllianceModifier();
            case RED_ALLIANCE:
                return new RedAllianceModifier();
            case ZONE_MODIFIER:
                return new ZoneModifier();
        }
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Get the modifier type of this modifier
     * 
     * @return the modifier type of this modifier
     */
    public abstract ObstacleModifierTypes getType();

    /**
     * Whether or not the obstacle is active at this time.
     * 
     * @return true if the obstacle is currently active, false if not
     */
    public abstract boolean isActive();

    /**
     * Whether or not this modifier MUST be active to have the obstacle active.
     * If true, will use the AND operator with other modifiers, if false, uses OR.
     * 
     * @return True if required to keep the obstacle active, false if not required.
     */
    public abstract boolean requiredForActive();

    /**
     * Get the current phase of the match
     * 
     * @return The current phase of the match
     */
    protected MatchPhase getCurrentPhase() {
        throw new UnsupportedOperationException("TODO");
    }
}