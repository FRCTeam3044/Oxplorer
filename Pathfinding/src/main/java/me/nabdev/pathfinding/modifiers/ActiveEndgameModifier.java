package me.nabdev.pathfinding.modifiers;

import me.nabdev.pathfinding.Pathfinder;
import me.nabdev.pathfinding.utilities.DriverStationWrapper;

/**
 * A modifier that keeps the obstacle active during the endgame period
 */
public class ActiveEndgameModifier extends ObstacleModifier {

    @Override
    public boolean isActive() {
        if (DriverStationWrapper.isTeleop()) {
            double matchTime = DriverStationWrapper.getMatchTime();
            if (matchTime <= Pathfinder.getEndgameTime()) {
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    @Override
    public boolean requiredForActive() {
        return false;
    }

    @Override
    public ObstacleModifierTypes getType() {
        return ObstacleModifierTypes.ACTIVE_ENDGAME;
    }
}
