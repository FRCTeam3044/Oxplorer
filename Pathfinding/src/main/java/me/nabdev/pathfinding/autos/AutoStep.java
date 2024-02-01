package me.nabdev.pathfinding.autos;

import org.json.JSONObject;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

public abstract class AutoStep extends SequentialCommandGroup {
    private JSONObject myParams;

    public AutoStep(JSONObject myParams) {
        this.myParams = myParams;
    };

    public abstract Command getCommand();
}
