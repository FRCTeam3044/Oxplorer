package me.nabdev.pathfinding.modifiers;

/**
 * A modifier that keeps the obstacle active at all times
 */
public class AlwaysActiveModifier extends ObstacleModifier {
    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public boolean requiredForActive() {
        return false;
    }

    @Override
    public ObstacleModifierTypes getType() {
        return ObstacleModifierTypes.ALWAYS_ACTIVE;
    }
}