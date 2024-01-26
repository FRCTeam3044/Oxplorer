package me.nabdev.pathfinding.modifiers;

import java.util.ArrayList;

import me.nabdev.pathfinding.modifiers.ObstacleModifier.ObstacleModifierTypes;
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
     * Whether or not the obstacle is active right now
     */
    private boolean isActive;

    /**
     * Whether or not the cache is invalid
     */
    private boolean cacheInvalid = true;

    /**
     * Create a new modifer collection and apply the given modifiers
     * 
     * @param modifiers The modifiers to apply
     */
    public ModifierCollection(JSONArray modifiers) {
        requiredModifiers = new ArrayList<>();
        optionalModifiers = new ArrayList<>();
        allModifiers = new ArrayList<>();
        for (int i = 0; i < modifiers.length(); i++) {
            ObstacleModifierTypes type = ObstacleModifierTypes.valueOf(modifiers.getString(i));
            ObstacleModifier mod = ObstacleModifier.getModifier(type, this);
            allModifiers.add(mod);
            if (mod.requiredForActive())
                requiredModifiers.add(mod);
            else
                optionalModifiers.add(mod);
        }
    }

    /**
     * Get whether or not this modifier collection has a specific obstacle modifier
     * 
     * @param modifierType The modifier type to check
     * @return true if the given modifier is present
     */
    public boolean hasModifier(ObstacleModifierTypes modifierType) {
        for (ObstacleModifier mod : allModifiers) {
            if (mod.getType() == modifierType)
                return true;
        }
        return false;
    }

    /**
     * Command a re-calculation for if the obstacle is active or not.
     */
    public void invalidateCache() {
        cacheInvalid = true;
    }

    /**
     * Whether or not the obstacle is active right now based on the modifiers
     * 
     * @return true if the obstacle is active
     */
    public boolean isActive() {
        if (cacheInvalid) {
            for (ObstacleModifier mod : requiredModifiers) {
                if (!mod.isActive())
                    isActive = false;
            }
            boolean hasOptional = false;
            for (ObstacleModifier mod : optionalModifiers) {
                if (mod.isActive())
                    hasOptional = true;
            }
            isActive = isActive && hasOptional;
            cacheInvalid = false;
        }
        return isActive;
    }
}