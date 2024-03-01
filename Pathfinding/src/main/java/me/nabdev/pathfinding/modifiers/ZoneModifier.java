package me.nabdev.pathfinding.modifiers;

/**
 * Marks the obstacle as a zone. This means it will not invalidate obstacle
 * points and start points will not snap outside of it.
 */
public class ZoneModifier extends ObstacleModifier {
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
        return ObstacleModifierTypes.ZONE_MODIFIER;
    }
}
