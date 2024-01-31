package me.nabdev.pathfinding.autos;

import org.json.JSONObject;
import edu.wpi.first.wpilibj2.command.Command;

public abstract class AutoStep {
    private JSONObject myParams;

    public AutoStep(JSONObject myParams) {
        this.myParams = myParams;
    };

    public abstract Command getCommand();
}
