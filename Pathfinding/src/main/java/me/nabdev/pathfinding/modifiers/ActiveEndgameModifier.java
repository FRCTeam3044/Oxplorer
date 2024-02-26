package me.nabdev.pathfinding.modifiers;

import edu.wpi.first.wpilibj.DriverStation;
import me.nabdev.pathfinding.Pathfinder;

/**
 * A modifier that keeps the obstacle active during the endgame period
 */
public class ActiveEndgameModifier extends ObstacleModifier {

    @Override
    public boolean isActive() {
        if (DriverStation.isTeleop()) {
            double matchTime = DriverStation.getMatchTime();
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
