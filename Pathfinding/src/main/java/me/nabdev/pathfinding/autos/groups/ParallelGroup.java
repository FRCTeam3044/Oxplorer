package me.nabdev.pathfinding.autos.groups;

import edu.wpi.first.wpilibj2.command.Commands;
import me.nabdev.pathfinding.autos.AutoGroup;
import edu.wpi.first.wpilibj2.command.Command;
import java.util.ArrayList;

/**
 * An AutoGroup that finishes when all commands finish
 */
public class ParallelGroup implements AutoGroup {
    public Command getCommand(ArrayList<Command> children) {
        Command[] commandArray = children.toArray(new Command[children.size()]);
        return Commands.parallel(commandArray);
    }
}