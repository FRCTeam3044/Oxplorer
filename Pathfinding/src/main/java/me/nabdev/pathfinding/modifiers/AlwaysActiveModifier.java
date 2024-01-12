package me.nabdev.pathfinding.modifiers;

public class AlwaysActiveModifier extends ObstacleModifier {
    @Override
    public boolean isActive(){
        return true;
    }

    @Override
    public boolean requiredForActive(){
        return false;
    }
}