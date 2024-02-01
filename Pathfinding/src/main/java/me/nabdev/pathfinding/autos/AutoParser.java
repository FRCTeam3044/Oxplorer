package me.nabdev.pathfinding.autos;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.wpi.first.wpilibj2.command.Command;

public class AutoParser {
    private HashMap<String, Command> commands;

    public void registerCommand(String name, Command command) {
        commands.put(name, command);
    }

    public Command parseAuto(JSONArray auto) {
        ArrayList<AutoStep> steps = new ArrayList<AutoStep>();
        for (int i = 0; i < auto.length(); i++) {
            JSONObject step = auto.getJSONObject(i);
            steps.add(parseStep(step));
        }
        return null;
    }

    public Command parseStep(JSONObject step) {
        String type = step.getString("type");
        String id = step.getString("id");
        JSONObject parameters = step.getJSONObject("parameters");

        if (type.equalsIgnoreCase("group")) {
            JSONArray children = step.getJSONArray("children");

            ArrayList<Command> parsedChildren = new ArrayList<Command>();
            for (int i = 0; i < children.length(); i++) {
                parsedChildren.add(parseStep(children.getJSONObject(i)));
            }
            return parseGroup(parsedChildren, id);
        } else if(type.equalsIgnoreCase("command")){
            // Generate the command
        }

        return null;
    }

    public Command parseGroup(ArrayList<Command> children, String id) {
        // Here, we create a command using the various sequence factories and return
        return null;
    }
}
