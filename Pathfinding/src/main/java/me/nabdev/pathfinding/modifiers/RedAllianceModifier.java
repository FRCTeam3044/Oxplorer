package me.nabdev.pathfinding.modifiers;

/**
 * A modifier that denotes the obstacle as the red alliance's
 */
public class RedAllianceModifier extends ObstacleModifier {
    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public boolean requiredForActive() {
        return false;
    }

    @Override
    public ObstacleModifierTypes getType() {
        return ObstacleModifierTypes.RED_ALLIANCE;
    }
}