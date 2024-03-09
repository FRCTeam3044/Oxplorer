package me.nabdev.pathfinding.autos.booleans;

import java.util.function.BooleanSupplier;

import me.nabdev.pathfinding.autos.AutoBoolean;

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
