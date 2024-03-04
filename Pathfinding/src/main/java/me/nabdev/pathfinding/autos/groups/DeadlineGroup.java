package me.nabdev.pathfinding.autos.groups;

import edu.wpi.first.wpilibj2.command.Commands;
import me.nabdev.pathfinding.autos.AutoGroup;
import edu.wpi.first.wpilibj2.command.Command;
import java.util.ArrayList;

/**
 * An auto group that only finishes when the first command in the array
 * finishes.
 */
public class DeadlineGroup implements AutoGroup {
    public Command getCommand(ArrayList<Command> children) {
        Command deadline = children.remove(0);
        Command[] commandArray = children.toArray(new Command[children.size()]);
        return Commands.deadline(deadline, commandArray);
    }
}