package frc.robot.subsystems.sim;

import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.simulation.DifferentialDrivetrainSim;
import edu.wpi.first.wpilibj.simulation.EncoderSim;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Constants.DriveConstants;
import frc.robot.subsystems.supers.DriveSuper;

public class SimDrive extends DriveSuper {
    public static DifferentialDrivetrainSim driveSim;
    /**
     * The left-side drive encoder
     */
    private final Encoder leftEncoder = new Encoder(0, 1, false);
    /**
     * The right-side drive encoder
     */
    private final Encoder rightEncoder = new Encoder(2, 3, false);
    public final Field2d field = new Field2d();
    // Simulation of drivetrain & encoders
    private EncoderSim leftEncoderSim;
    private EncoderSim rightEncoderSim;

    /**
     * This method is used to create the simulation of the drive base. It is called by the constructor of this class.
     */
    public SimDrive() {
        // Instantiate the super class which will handle most setup
        super();
        createSimDrive();
        createSimEncoders();
    }

    /**
     * This method is used to simulate the robot. It is called every 20ms by the simulationPeriodic method of
     * TimedRobot.
     */
    @Override
    public void simulationPeriodic() {
        simulateDrivetrain();
        simulateEncoders();
        renderField();
    }

    private void createSimDrive() {
        driveSim = new DifferentialDrivetrainSim(
                DriveConstants.k_drivetrainPlant,
                DriveConstants.k_driveMotor,
                DriveConstants.k_driveGearing,
                DriveConstants.k_trackWidth,
                DriveConstants.k_wheelDiameterMeters / 2,
                VecBuilder.fill(0.005, 0.005, 0.0001, 0.05, 0.05, 0.005, 0.005)
        );

        driveSim.setPose(driveOdometry.getPoseMeters());
    }

    /**
     * This method is used to create the encoder sims. It is called by the constructor of this class.
     */
    private void createSimEncoders() {
        leftEncoder.setDistancePerPulse(42);
        rightEncoder.setDistancePerPulse(42);
        leftEncoderSim = new EncoderSim(leftEncoder);
        rightEncoderSim = new EncoderSim(rightEncoder);
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

    /**
     * This method is used to simulate the drive bases movement. It is called every 20ms by the simulationPeriodic
     * method.
     */
    private void simulateDrivetrain() {
        driveSim.setInputs(
                leftMotors.get() * RobotController.getBatteryVoltage(),
                rightMotors.get() * RobotController.getBatteryVoltage()
        );
        driveSim.update(0.02);
    }

    /**
     * This method is used to simulate the encoders on the drive base. It is called every 20ms by the simulationPeriodic
     * method.
     */
    private void simulateEncoders() {
        leftEncoderSim.setDistance(driveSim.getLeftPositionMeters());
        leftEncoderSim.setRate(driveSim.getLeftVelocityMetersPerSecond());
        rightEncoderSim.setDistance(driveSim.getRightPositionMeters());
        rightEncoderSim.setRate(driveSim.getRightVelocityMetersPerSecond());
    }

    /**
     * This method is used to push the drivetrain simulation to network tables so that it can be viewed in the
     * simulation GUI & other rendering clients.
     */
    private void renderField() {
        field.setRobotPose(driveSim.getPose());

        SmartDashboard.putData("Field", field);
    }
}
