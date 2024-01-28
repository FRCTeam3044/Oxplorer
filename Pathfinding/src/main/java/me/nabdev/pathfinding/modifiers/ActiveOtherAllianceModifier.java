package me.nabdev.pathfinding.modifiers;

import java.util.Optional;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;

/**
 * A modifier that keeps the obstacle active if it is not the same alliance as
 * the robot
 */
public class ActiveOtherAllianceModifier extends ObstacleModifier {
    ModifierCollection myCollection;

    /**
     * Creates a new ActiveOtherAllianceModifier
     * 
     * @param collection The modifier collection associated with this modifier
     */
    public ActiveOtherAllianceModifier(ModifierCollection collection) {
        myCollection = collection;
    }

    @Override
    public boolean isActive() {
        Optional<Alliance> alliance = DriverStation.getAlliance();
        if (!alliance.isPresent())
            return false;
        if (alliance.get() == DriverStation.Alliance.Blue) {
            return myCollection.hasModifier(ObstacleModifierTypes.RED_ALLIANCE);
        } else {
            return myCollection.hasModifier(ObstacleModifierTypes.BLUE_ALLIANCE);
        }
    }

    @Override
    public boolean requiredForActive() {
        return true;
    }

    @Override
    public ObstacleModifierTypes getType() {
        return ObstacleModifierTypes.ACTIVE_OTHER_ALLIANCE;
    }
}