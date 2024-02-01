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

    public AutoStep parseStep(JSONObject step) {
        String type = step.getString("type");
        String id = step.getString("id");
        JSONObject parameters = step.getJSONObject("parameters");
        JSONArray children = step.getJSONArray("children");

        ArrayList<AutoStep> parsedChildren = new ArrayList<AutoStep>();
        for (int i = 0; i < children.length(); i++) {
            parsedChildren.add(parseStep(children.getJSONObject(i)));
        }

        if (type.equalsIgnoreCase("simultaneous")) {
            return parseSimultaneous(parsedChildren, id);
        }

        return null;
    }

    public AutoStep parseSimultaneous(ArrayList<AutoStep> commands, String id) {
        return null;
    }
}
