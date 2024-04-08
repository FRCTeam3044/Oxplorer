package me.nabdev.pathfinding.modifiers;

import me.nabdev.pathfinding.utilities.DriverStationWrapper;

/**
 * A modifier that keeps the obstacle active during the autonomous period
 */
public class ActiveAutoModifier extends ObstacleModifier {
    @Override
    public boolean isActive() {
        return DriverStationWrapper.isAutonomous();
    }

    @Override
    public boolean requiredForActive() {
        return false;
    }

    @Override
    public ObstacleModifierTypes getType() {
        return ObstacleModifierTypes.ACTIVE_AUTO;
    }
}
