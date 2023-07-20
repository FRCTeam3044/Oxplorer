package frc.robot.commands.drive;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.RobotContainer;
import frc.robot.subsystems.supers.DriveSuper;

public class CurvatureDrive extends CommandBase {
    private final DriveSuper subsystem;

    /**
     * Creates a new Arcade Drive Command.
     *
     * @param drive The Drive Super subsystem used by this command.
     */
    public CurvatureDrive(DriveSuper drive) {
        this.subsystem = drive;
        // Use addRequirements() here to declare subsystem dependencies.
        addRequirements(drive);
    }

    // Called when the command is initially scheduled.
    @Override
    public void initialize() {
        System.out.println("Curvature Drive initialized");
    }

    // Called every time the scheduler runs while the command is scheduled.
    @Override
    public void execute() {
        subsystem.drive.curvatureDrive(
                -RobotContainer.driverController.getLeftY(),
                -RobotContainer.driverController.getRightX(),
                true
        );
    }
}
