package me.nabdev.pathfinding.utilities;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;

import java.util.Optional;

/**
 * A wrapper class for the DriverStation class that allows for the use of the
 * DriverStation class in an environment where wpiHalJNI is unavailable (I.E.
 * OxplorerGUI). Returns default values if -Dnohaljni=true is given to the JVM.
 */
public class DriverStationWrapper {
    private static boolean canUseJni;
    static {
        canUseJni = !Boolean.getBoolean("nohaljni");
    }

    /**
     * Returns whether the Driver Station is currently enabled. False if HALJNI is
     * disabled.
     * 
     * @return true if the Driver Station is currently enabled, false otherwise
     */
    public static boolean isAutonomous() {
        if (canUseJni) {
            return DriverStation.isAutonomous();
        } else {
            return false;
        }
    }

    /**
     * Returns whether the Driver Station is currently enabled. True if HALJNI is
     * disabled.
     * 
     * @return true if the Driver Station is currently enabled, false otherwise
     */
    public static boolean isTeleop() {
        if (canUseJni) {
            return DriverStation.isTeleop();
        } else {
            return true;
        }
    }

    /**
     * Returns whether the Driver Station is currently enabled. False if HALJNI is
     * disabled.
     * 
     * @return true if the Driver Station is currently enabled, false otherwise
     */
    public static double getMatchTime() {
        if (canUseJni) {
            return DriverStation.getMatchTime();
        } else {
            return 0;
        }
    }

    /**
     * Returns the alliance that the driver station is on. If HALJNI is disabled,
     * returns an empty Optional.
     * 
     * @return the alliance that the driver station is on
     */
    public static Optional<Alliance> getAlliance() {
        if (canUseJni) {
            return DriverStation.getAlliance();
        } else {
            return Optional.empty();
        }
    }
}
