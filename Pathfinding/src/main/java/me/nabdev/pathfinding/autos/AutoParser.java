package me.nabdev.pathfinding.autos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;
import java.lang.IllegalArgumentException;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.wpi.first.wpilibj2.command.Command;

/**
 * Converts json autos to commands which can be scheduled.
 */
public class AutoParser {
    private static HashMap<String, Function<JSONObject, Command>> commands = new HashMap<String, Function<JSONObject, Command>>();
    private static HashMap<String, AutoGroup> groups = new HashMap<String, AutoGroup>();

    static {
        registerGroupType("deadline", new DeadlineGroup());
        registerGroupType("race", new RaceGroup());
    }
    /**
     * Register a command which can be used in the autos.
     * 
     * @param id The id of the command, which will be used in the auto json file
     * @param command A function that takes in a JSONObject of parameters and returns a Command.
     */
    public static void registerCommand(String id, Function<JSONObject, Command> command) {
        commands.put(id, command);
    }

    /**
     * Add a group type to use in autos.
     * 
     * @param id The id of the group, which will be used in the auto json file
     * @param group The autogroup instance to use
     */
    public static void registerGroupType(String id, AutoGroup group){
        groups.put(id, group);
    }

    /**
     * Parse an auto from a JSONArray and return a command to run.
     * 
     * @param auto The auto to parse
     * @return The auto command
     */
    public static Command parseAuto(JSONArray auto) {
        ArrayList<Command> steps = new ArrayList<Command>();
        for (int i = 0; i < auto.length(); i++) {
            JSONObject step = auto.getJSONObject(i);
            steps.add(parseStep(step));
        }
        Command chainCommand = steps.get(0);
        for(int i = 1; i < steps.size(); i++){
            Command step = steps.get(i);
            chainCommand = chainCommand.andThen(step);
        }
        return chainCommand;
    }

    /**
     * Parses one step of the auto (recursive)
     * 
     * @param step The step to parse 
     */
    static Command parseStep(JSONObject step) {
        String type = step.getString("type");
        String id = step.getString("id");
        JSONObject parameters = step.getJSONObject("parameters");

        if (type.equalsIgnoreCase("group")) {
            JSONArray children = step.getJSONArray("children");

            ArrayList<Command> parsedChildren = new ArrayList<Command>();
            for (int i = 0; i < children.length(); i++) {
                parsedChildren.add(parseStep(children.getJSONObject(i)));
            }
            if(!groups.containsKey(id)){
                throw new IllegalArgumentException("Invalid auto group id: " + id + ". Did you forget to register it?");
            }
            AutoGroup group = groups.get(id);
            return group.getCommand(parsedChildren);
        } else if(type.equalsIgnoreCase("command")){
            if(!commands.containsKey(id)){
                throw new IllegalArgumentException("Invalid command id: " + id + ". Did you forget to register it?");
            }
            return commands.get(id).apply(parameters);
        } else {
            throw new IllegalArgumentException("Invalid auto segment type: " + type);
        }
    }
}
