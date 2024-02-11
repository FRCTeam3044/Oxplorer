package me.nabdev.pathfinding.autos;

import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.Command;
import java.util.ArrayList;

/**
 * An auto group that only finishes when the first command in the array finishes.
 */
public class DeadlineGroup implements AutoGroup {
    public Command getCommand(ArrayList<Command> children){
        Command deadline = children.remove(0);
        Command[] commandArray = children.toArray(new Command[children.size()]);
        return Commands.deadline(deadline, commandArray);
    }
}