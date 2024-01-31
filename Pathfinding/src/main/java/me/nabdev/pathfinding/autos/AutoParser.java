package me.nabdev.pathfinding.autos;

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
        for (int i = 0; i < auto.length(); i++) {
            JSONObject step = auto.getJSONObject(i);

        }
        return null;
    }
}
