package me.nabdev.pathfinding.modifiers;

import edu.wpi.first.wpilibj.DriverStation;
import me.nabdev.pathfinding.Pathfinder;

/**
 * A modifier that keeps the obstacle active during the teleop period, not
 * including endgame
 */
public class ActiveTeleModifier extends ObstacleModifier {
    @Override
    public boolean isActive() {
        if (DriverStation.isTeleop()) {
            double matchTime = DriverStation.getMatchTime();
            if (matchTime <= Pathfinder.getEndgameTime()) {
                return false;
            }
            return true;
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
        return ObstacleModifierTypes.ACTIVE_TELE;
    }
}
