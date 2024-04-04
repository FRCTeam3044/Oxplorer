package me.nabdev.pathfinding.modifiers;

import java.util.Optional;

import edu.wpi.first.wpilibj.DriverStation.Alliance;
import me.nabdev.pathfinding.utilities.DriverStationWrapper;

/**
 * A modifier that keeps the obstacle active if it is the same alliance as the
 * robot
 */
public class ActiveMyAllianceModifier extends ObstacleModifier {
    ModifierCollection myCollection;

    /**
     * Creates a new ActiveMyAllianceModifier
     * 
     * @param collection The modifier collection associated with this modifier
     */
    public ActiveMyAllianceModifier(ModifierCollection collection) {
        myCollection = collection;
    }

    @Override
    public boolean isActive() {
        Optional<Alliance> alliance = DriverStationWrapper.getAlliance();
        if (!alliance.isPresent())
            return false;
        if (alliance.get() == Alliance.Blue) {
            return myCollection.hasModifier(ObstacleModifierTypes.BLUE_ALLIANCE);
        } else {
            return myCollection.hasModifier(ObstacleModifierTypes.RED_ALLIANCE);
        }
    }

    @Override
    public boolean requiredForActive() {
        return true;
    }

    @Override
    public ObstacleModifierTypes getType() {
        return ObstacleModifierTypes.ACTIVE_MY_ALLIANCE;
    }
}