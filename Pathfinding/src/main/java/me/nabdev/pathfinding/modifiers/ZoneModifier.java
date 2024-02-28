package me.nabdev.pathfinding.modifiers;

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
