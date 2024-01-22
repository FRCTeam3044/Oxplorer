import java.util.ArrayList;

import me.nabdev.pathfinding.structures.Obstacle;
import org.json.JSONArray;

/**
 * Represents all the modifiers added to a single obstacle
 */
public class ModifierCollection {
    /**
     * Modifers required for the obstacle to be active
     */
    private ArrayList<ObstacleModifier> requiredModifiers;

    /**
     * Modifiers where only one is required for the obstacle to be active
     */
    private ArrayList<ObstacleModifier> optionalModifiers;

    /**
     * All modifiers on this obstacle
     */
    private ArrayList<ObstacleModifier> allModifiers;

    /**
     * Create a new modifer collection and apply the given modifiers
     * 
     * @pararm modifers The modifiers to apply
     */
    public ModifierCollection(JSONArray modifiers){

    }

    /**
     * Get whether or not this modifier collection has a specific obstacle
     * 
     * @param modifierType The modifier type to check
     * @return true if the given modifier is present
     */
    public boolean hasModifier(ObstacleModifierTypes modifierType){
        for(ObstacleModifier mod : allModifiers){
            if(mod.getType() == modifierType) return true;
        }
        return false;
    }
}