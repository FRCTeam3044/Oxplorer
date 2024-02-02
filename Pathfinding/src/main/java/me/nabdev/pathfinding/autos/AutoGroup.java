package me.nabdev.pathfinding.autos;

import java.util.ArrayList;
import edu.wpi.first.wpilibj2.command.Command;

public interface AutoGroup {
    public Command getCommand(ArrayList<Command> children);
}
