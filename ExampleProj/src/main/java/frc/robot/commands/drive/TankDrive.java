package frc.robot.commands.drive;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.RobotContainer;
import frc.robot.subsystems.supers.DriveSuper;

public class TankDrive extends CommandBase {
    private final DriveSuper subsystem;

    /**
     * Creates a new Arcade Drive Command.
     *
     * @param subsystem The Drive Super subsystem used by this command.
     */
    public TankDrive(DriveSuper subsystem) {
        this.subsystem = subsystem;
        // Use addRequirements() here to declare subsystem dependencies.
        addRequirements(subsystem);
    }

    // Called when the command is initially scheduled.
    @Override
    public void initialize() {
        System.out.println("Tank Drive initialized");
    }

    // Called every time the scheduler runs while the command is scheduled.
    @Override
    public void execute() {
        subsystem.drive.tankDrive(
                -RobotContainer.driverController.getLeftY(),
                -RobotContainer.driverController.getRightY(),
                true
        );
    }
}
