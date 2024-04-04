package me.nabdev.pathfinding.utilities;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;

import java.util.Optional;

public class DriverStationWrapper {
    private static boolean canUseJni;
    static {
        canUseJni = !Boolean.getBoolean("nohaljni");
    }

    public static boolean isAutonomous() {
        if (canUseJni) {
            return DriverStation.isAutonomous();
        } else {
            return false;
        }
    }

    public static boolean isTeleop() {
        if (canUseJni) {
            return DriverStation.isTeleop();
        } else {
            return true;
        }
    }

    public static double getMatchTime() {
        if (canUseJni) {
            return DriverStation.getMatchTime();
        } else {
            return 0;
        }
    }

    public static Optional<Alliance> getAlliance() {
        if (canUseJni) {
            return DriverStation.getAlliance();
        } else {
            return Optional.empty();
        }
    }
}
