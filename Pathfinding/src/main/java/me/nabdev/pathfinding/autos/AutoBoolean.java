package me.nabdev.pathfinding.autos;

import java.util.function.BooleanSupplier;

/**
 * Represents a way of conditionally running commands in an auto group.
 */
public interface AutoBoolean {
    /**
     * Get the boolean supplier for this AutoBoolean
     * 
     * @param children The boolean suppliers to use in the group
     * @return The boolean supplier for this AutoBoolean
     */
    public BooleanSupplier getSupplier(BooleanSupplier... children);
}
