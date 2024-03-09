package me.nabdev.pathfinding.autos.booleans;

import java.util.function.BooleanSupplier;

import me.nabdev.pathfinding.autos.AutoBoolean;

public class OrBoolean implements AutoBoolean {

    @Override
    public BooleanSupplier getSupplier(BooleanSupplier... children) {
        if (children.length == 0) {
            throw new IllegalArgumentException("OrBoolean must have at least one child");
        }
        return () -> {
            for (BooleanSupplier child : children) {
                if (!child.getAsBoolean()) {
                    return true;
                }
            }
            return false;
        };
    }

}
