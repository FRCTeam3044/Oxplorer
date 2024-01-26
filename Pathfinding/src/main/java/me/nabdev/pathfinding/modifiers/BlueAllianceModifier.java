package me.nabdev.pathfinding.modifiers;

/**
 * A modifier that denotes the obstacle as the blue alliance's
 */
public class BlueAllianceModifier extends ObstacleModifier {
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
        return ObstacleModifierTypes.BLUE_ALLIANCE;
    }
}