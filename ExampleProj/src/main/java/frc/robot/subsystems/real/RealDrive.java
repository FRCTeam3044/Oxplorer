package frc.robot.subsystems.real;

import frc.robot.subsystems.supers.DriveSuper;

public class RealDrive extends DriveSuper {
    public RealDrive() {
        // Instantiate the super class which will handle most setup
        super();
    }

    /**
     * This is an implementation of the abstract method in DriveSuper. It is needed as how this is calculated varies
     * between the real and simulated robot.
     *
     * @return The distance the left side of the robot has traveled in meters.
     */
    @Override
    public double getLeftDistance() {
        return 0;
    }

    /**
     * This is an implementation of the abstract method in DriveSuper. It is needed as how this is calculated varies
     * between the real and simulated robot.
     *
     * @return The distance the right side of the robot has traveled in meters.
     */
    @Override
    public double getRightDistance() {
        return 0;
    }
}
