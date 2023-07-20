package frc.robot.subsystems.supers;

import com.revrobotics.CANSparkMax;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.DifferentialDriveOdometry;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.DriveConstants;

public abstract class DriveSuper extends SubsystemBase {
    // The motor controllers and control groups for the drive base
    public static final CANSparkMax leftFront = new CANSparkMax(11, CANSparkMax.MotorType.kBrushless);
    public static final CANSparkMax leftRear = new CANSparkMax(12, CANSparkMax.MotorType.kBrushless);
    public static final MotorControllerGroup leftMotors = new MotorControllerGroup(leftFront, leftRear);
    public static final CANSparkMax rightFront = new CANSparkMax(13, CANSparkMax.MotorType.kBrushless);
    public static final CANSparkMax rightRear = new CANSparkMax(14, CANSparkMax.MotorType.kBrushless);
    public static final MotorControllerGroup rightMotors = new MotorControllerGroup(rightFront, rightRear);
    /**
     * The odometry object that will be used to track the position of the robot.
     */
    public static final DifferentialDriveOdometry driveOdometry = new DifferentialDriveOdometry(
            new Rotation2d(0), 0, 0
    );
    /**
     * The differential drive object that will be used to control the drive base with wpilibs built in methods.
     */
    public final DifferentialDrive drive = new DifferentialDrive(leftMotors, rightMotors);

    /**
     * The constructor for the DriveSuper class. This is called by the constructors of the RealDrive and SimDrive
     * classes. It handles the setup of the motors, the differential drive, and other shared functionality.
     */
    public DriveSuper() {
        configureMotors();
    }

    /**
     * Sets the inversion of the motor controllers, sets the current limits to protect the motors, and sets the idle
     * mode to brake to prevent the robot from rolling around and being pushed.
     */
    private void configureMotors() {
        leftFront.setInverted(false);
        leftRear.setInverted(false);
        rightFront.setInverted(true);
        rightRear.setInverted(true);

        leftFront.setSmartCurrentLimit(DriveConstants.k_currentLimit);
        leftRear.setSmartCurrentLimit(DriveConstants.k_currentLimit);
        rightFront.setSmartCurrentLimit(DriveConstants.k_currentLimit);
        rightRear.setSmartCurrentLimit(DriveConstants.k_currentLimit);

        leftFront.setIdleMode(DriveConstants.k_idleMode);
        leftRear.setIdleMode(DriveConstants.k_idleMode);
        rightFront.setIdleMode(DriveConstants.k_idleMode);
        rightRear.setIdleMode(DriveConstants.k_idleMode);
    }

    /**
     * @return The distance traveled by the left side of the robot in meters.
     */
    public abstract double getLeftDistance();

    /**
     * @return The distance traveled by the right side of the robot in meters.
     */
    public abstract double getRightDistance();
}
