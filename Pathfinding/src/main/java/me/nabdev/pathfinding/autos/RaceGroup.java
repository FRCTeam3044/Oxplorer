package me.nabdev.pathfinding.autos;

import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.Command;
import java.util.ArrayList;

/**
 * An AutoGroup that finishes when any command finishes.
 */
public class RaceGroup implements AutoGroup {
    public Command getCommand(ArrayList<Command> children){
        Command[] commandArray = children.toArray(new Command[children.size()]);
        return Commands.race(commandArray);
    }
}