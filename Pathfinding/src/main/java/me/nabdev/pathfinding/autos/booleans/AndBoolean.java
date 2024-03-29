package me.nabdev.pathfinding.autos.booleans;

import java.util.function.BooleanSupplier;

import me.nabdev.pathfinding.autos.AutoBoolean;

/**
 * An auto boolean that returns true if all of its children are true
 */
public class AndBoolean implements AutoBoolean {
    @Override
    public BooleanSupplier getSupplier(BooleanSupplier... children) {
        if (children.length == 0) {
            throw new IllegalArgumentException("AndBoolean must have at least one child");
        }
        return () -> {
            for (BooleanSupplier child : children) {
                if (!child.getAsBoolean()) {
                    return false;
                }
            }
            return true;
        };
    }

}
