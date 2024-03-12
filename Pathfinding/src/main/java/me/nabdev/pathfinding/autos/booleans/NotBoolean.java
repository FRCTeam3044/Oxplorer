package me.nabdev.pathfinding.autos.booleans;

import java.util.function.BooleanSupplier;

import me.nabdev.pathfinding.autos.AutoBoolean;

/**
 * An auto boolean that returns true if its child is false
 */
public class NotBoolean implements AutoBoolean {

    @Override
    public BooleanSupplier getSupplier(BooleanSupplier... children) {
        if (children.length != 1) {
            throw new IllegalArgumentException("NotBoolean must have exactly one child");
        }
        return () -> {
            return !children[0].getAsBoolean();
        };
    }
}
