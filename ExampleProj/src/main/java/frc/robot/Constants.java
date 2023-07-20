// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.revrobotics.CANSparkMax;
import edu.wpi.first.math.numbers.N2;
import edu.wpi.first.math.system.LinearSystem;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.system.plant.LinearSystemId;

/**
 * The Constants class provides a convenient place for teams to hold robot-wide numerical or boolean
 * constants. This class should not be used for any other purpose. All constants should be declared
 * globally (i.e. public static). Do not put anything functional in this class.
 *
 * <p>It is advised to statically import this class (or one of its inner classes) wherever the
 * constants are needed, to reduce verbosity.
 */
public final class Constants {
    public static class PathfindingConstants {
        public static final double clearance = 1.0;
    }
    public static class OperatorConstants {
        public static final int k_driverControllerPort = 0;
    }

    public static class DriveConstants {
        // Spark Max Setup
        public static final int k_currentLimit = 40;
        public static final CANSparkMax.IdleMode k_idleMode = CANSparkMax.IdleMode.kBrake;

        // Kinematics & Estimation
        public static final double k_trackWidth = 0.5900928;
        public static final double k_wheelDiameterMeters = 0.15;
        public static final double k_driveGearing = 8.25;

        // Simulation
        public static final DCMotor k_driveMotor = DCMotor.getNEO(2);

        // These values are calculated using Sysid, documentation for how to do this is found in docs/CALIBRATING.md
        public static final double kvVoltSecondsPerMeter = 3;
        public static final double kaVoltSecondsSquaredPerMeter = 0.3;
        public static final double kvVoltSecondsPerRadian = 2;
        public static final double kaVoltSecondsSquaredPerRadian = 0.4;
        public static final LinearSystem<N2, N2, N2> k_drivetrainPlant = LinearSystemId.identifyDrivetrainSystem(
                kvVoltSecondsPerMeter,
                kaVoltSecondsSquaredPerMeter,
                kvVoltSecondsPerRadian,
                kaVoltSecondsSquaredPerRadian);
    }
}
