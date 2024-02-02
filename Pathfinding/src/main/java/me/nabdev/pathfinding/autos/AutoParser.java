package me.nabdev.pathfinding.autos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;
import java.lang.IllegalArgumentException;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.wpi.first.wpilibj2.command.Command;

public class AutoParser {
    private static HashMap<String, Function<JSONObject, Command>> commands = new HashMap<String, Function<JSONObject, Command>>();
    private static HashMap<String, AutoGroup> groups = new HashMap<String, AutoGroup>();

    public static void registerCommand(String name, Function<JSONObject, Command> command) {
        commands.put(name, command);
    }

    public static void registerGroupType(String id, AutoGroup group){
        groups.put(id, group);
    }

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

    public static Command parseStep(JSONObject step) {
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
