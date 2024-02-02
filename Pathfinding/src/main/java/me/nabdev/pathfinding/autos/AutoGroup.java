package me.nabdev.pathfinding.autos;

import java.util.ArrayList;
import edu.wpi.first.wpilibj2.command.Command;

/**
 * Represents a way of sequencing commands in an auto
 */
public interface AutoGroup {
    /**
     * Generate the command from the autogroup
     * 
     * @param children The commands to pass to the group
     * @return The command sequence made by the group
     */
    public Command getCommand(ArrayList<Command> children);
}
