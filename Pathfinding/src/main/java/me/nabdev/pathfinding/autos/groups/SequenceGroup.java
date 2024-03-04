package me.nabdev.pathfinding.autos.groups;

import edu.wpi.first.wpilibj2.command.Commands;
import me.nabdev.pathfinding.autos.AutoGroup;
import edu.wpi.first.wpilibj2.command.Command;
import java.util.ArrayList;

/**
 * SequenceGroup is an AutoGroup that runs commands in sequence.
 */
public class SequenceGroup implements AutoGroup {
    public Command getCommand(ArrayList<Command> children) {
        Command[] commandArray = children.toArray(new Command[children.size()]);
        return Commands.sequence(commandArray);
    }
}
