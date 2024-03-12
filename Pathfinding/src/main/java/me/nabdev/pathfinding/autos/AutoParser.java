package me.nabdev.pathfinding.autos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;
import java.util.function.BooleanSupplier;
import java.lang.IllegalArgumentException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import me.nabdev.pathfinding.autos.booleans.AndBoolean;
import me.nabdev.pathfinding.autos.booleans.NotBoolean;
import me.nabdev.pathfinding.autos.booleans.OrBoolean;
import me.nabdev.pathfinding.autos.groups.DeadlineGroup;
import me.nabdev.pathfinding.autos.groups.ParallelGroup;
import me.nabdev.pathfinding.autos.groups.RaceGroup;
import me.nabdev.pathfinding.autos.groups.SequenceGroup;

/**
 * Converts json autos to commands which can be scheduled.
 */
public class AutoParser {
    private static HashMap<String, Function<JSONObject, Command>> commands = new HashMap<String, Function<JSONObject, Command>>();
    private static HashMap<String, AutoGroup> groups = new HashMap<String, AutoGroup>();
    private static HashMap<String, String> macros = new HashMap<String, String>();
    private static HashMap<String, Function<JSONObject, AutoBoolean>> booleans = new HashMap<String, Function<JSONObject, AutoBoolean>>();

    static {
        registerGroupType("deadline", new DeadlineGroup());
        registerGroupType("race", new RaceGroup());
        registerGroupType("parallel", new ParallelGroup());
        registerGroupType("sequence", new SequenceGroup());
        registerBoolean("and", (JSONObject $) -> new AndBoolean());
        registerBoolean("or", (JSONObject $) -> new OrBoolean());
        registerBoolean("not", (JSONObject $) -> new NotBoolean());
    }

    /**
     * Register a command which can be used in the autos.
     * 
     * @param id      The id of the command, which will be used in the auto json
     *                file
     * @param command A function that takes in a JSONObject of parameters and
     *                returns a Command.
     */
    public static void registerCommand(String id, Function<JSONObject, Command> command) {
        commands.put(id, command);
    }

    /**
     * Add a group type to use in autos.
     * 
     * @param id    The id of the group, which will be used in the auto json file
     * @param group The autogroup instance to use
     */
    public static void registerGroupType(String id, AutoGroup group) {
        groups.put(id, group);
    }

    /**
     * Register a macro to use in autos.
     * 
     * @param id   The id of the macro, which will be used in the auto json file
     * @param name The name of the macro file, in deploy/autos/macros
     */
    public static void registerMacro(String id, String name) {
        String path = Filesystem.getDeployDirectory() + "/autos/macros/" + name;
        try {
            FileInputStream input = new FileInputStream(path);
            String macro = new String(input.readAllBytes());
            macros.put(id, macro);
            input.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Could not find macro file: " + name);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Could not read macro file: " + name);
        }
    }

    /**
     * Register a boolean to use in autos.
     * 
     * @param id   The id of the boolean, which will be used in the auto json file
     * @param bool A function that takes in a JSONObject of parameters and returns
     *             an AutoBoolean
     */
    public static void registerBoolean(String id, Function<JSONObject, AutoBoolean> bool) {
        booleans.put(id, bool);
    }

    /**
     * Load an auto from a json file in the deploy directory
     * 
     * @param name The name of the json file
     * @return The autonomous command
     * @throws FileNotFoundException if the file could not be found
     * @throws JSONException         if the auto is invalid
     * @throws IOException           if the macro could not be used
     */
    public static Command loadAuto(String name) throws FileNotFoundException, JSONException, IOException {
        String path = Filesystem.getDeployDirectory() + "/autos/" + name;
        FileInputStream input = new FileInputStream(path);
        JSONTokener tokener = new JSONTokener(input);
        return parseAuto(new JSONArray(tokener));
    }

    /**
     * Load an auto from a json file at the given path
     * 
     * @param path The path of the json auto
     * @return The autonomous command
     * @throws FileNotFoundException if the file could not be found
     * @throws JSONException         if the auto is invalid
     * @throws IOException           if the macro could not be used
     */
    public static Command loadAutoFromPath(String path) throws FileNotFoundException, JSONException, IOException {
        FileInputStream input = new FileInputStream(path);
        JSONTokener tokener = new JSONTokener(input);
        return parseAuto(new JSONArray(tokener));
    }

    /**
     * Parse an auto from a JSONArray and return a command to run.
     * 
     * @param auto The auto to parse
     * @return The auto command
     * @throws IOException   if the macro could not be used
     * @throws JSONException if the auto is invalid
     */
    public static Command parseAuto(JSONArray auto) throws JSONException, IOException {
        if (auto.length() < 1) {
            throw new IllegalArgumentException("Given Empty Autonomous!");
        }
        ArrayList<Command> steps = new ArrayList<Command>();
        for (int i = 0; i < auto.length(); i++) {
            JSONObject step = auto.getJSONObject(i);
            steps.add(parseStep(step));
        }
        Command[] commandArray = steps.toArray(new Command[steps.size()]);
        return Commands.sequence(commandArray);
    }

    /**
     * Parses one step of the auto (recursive)
     * 
     * @param step The step to parse
     * @throws IOException   if the macro could not be used
     * @throws JSONException if the step is invalid
     */
    static Command parseStep(JSONObject step) throws JSONException, IOException {
        String type = step.getString("type");

        if (type.equalsIgnoreCase("group")) {
            String id = step.getString("id");
            JSONArray children = step.getJSONArray("children");

            ArrayList<Command> parsedChildren = new ArrayList<Command>();
            for (int i = 0; i < children.length(); i++) {
                parsedChildren.add(parseStep(children.getJSONObject(i)));
            }
            if (!groups.containsKey(id)) {
                throw new IllegalArgumentException("Invalid auto group id: " + id + ". Did you forget to register it?");
            }
            AutoGroup group = groups.get(id);
            return group.getCommand(parsedChildren);
        } else if (type.equalsIgnoreCase("command")) {
            String id = step.getString("id");
            if (!commands.containsKey(id)) {
                throw new IllegalArgumentException("Invalid command id: " + id + ". Did you forget to register it?");
            }
            JSONObject parameters = step.getJSONObject("parameters");
            return commands.get(id).apply(parameters);
        } else if (type.equalsIgnoreCase("macro")) {
            String id = step.getString("id");
            if (!macros.containsKey(id)) {
                throw new IllegalArgumentException("Invalid macro id: " + id + ". Did you forget to register it?");
            }
            String macro = macros.get(id);
            JSONObject parameters = step.getJSONObject("parameters");
            for (String key : parameters.keySet()) {
                Object value = parameters.get(key);
                macro = macro.replace("{{ " + key + " }}", value.toString());
            }
            JSONArray filledInArray = new JSONArray(macro);
            return parseAuto(filledInArray);
        } else if (type.equalsIgnoreCase("if")) {
            JSONObject child = step.getJSONObject("child");
            JSONObject condition = step.getJSONObject("condition");
            Command parsedChild = parseStep(child);
            BooleanSupplier parsedCondition = parseBoolean(condition);
            return parsedChild.onlyIf(parsedCondition);
        } else if (type.equalsIgnoreCase("while")) {
            JSONObject child = step.getJSONObject("child");
            JSONObject condition = step.getJSONObject("condition");
            Command parsedChild = parseStep(child);
            BooleanSupplier parsedCondition = parseBoolean(condition);
            return parsedChild.onlyWhile(parsedCondition);
        } else {
            throw new IllegalArgumentException("Invalid auto segment type: " + type);
        }
    }

    public static BooleanSupplier parseBoolean(JSONObject bool) {
        String id = bool.getString("id");
        if (!booleans.containsKey(id)) {
            throw new IllegalArgumentException("Invalid boolean id: " + id + ". Did you forget to register it?");
        }
        JSONArray children = bool.getJSONArray("children");
        ArrayList<BooleanSupplier> parsedChildren = new ArrayList<BooleanSupplier>();
        for (int i = 0; i < children.length(); i++) {
            parsedChildren.add(parseBoolean(children.getJSONObject(i)));
        }
        JSONObject parameters = bool.getJSONObject("parameters");
        return booleans.get(id).apply(parameters)
                .getSupplier(parsedChildren.toArray(new BooleanSupplier[parsedChildren.size()]));
    }
}
