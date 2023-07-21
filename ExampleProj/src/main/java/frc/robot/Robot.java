// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONTokener;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.math.trajectory.TrajectoryGenerator;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.Constants.PathfindingConstants;
import frc.robot.subsystems.sim.SimDrive;
import me.nabdev.pathfinding.Pathfinder;
import me.nabdev.pathfinding.FieldLoader.Field;
import me.nabdev.pathfinding.Pathfinder.PathfindSnapMode;
import me.nabdev.pathfinding.Structures.ImpossiblePathException;
import me.nabdev.pathfinding.Structures.Path;
import me.nabdev.pathfinding.Structures.Vertex;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
    private Command m_autonomousCommand;

    private RobotContainer m_robotContainer;
    private Pathfinder pathfinder;

    private double[] lastStart = {3.5, 3};
    private double[] lastEnd = {8, 4};

    /**
     * This function is run when the robot is first started up and should be used for any
     * initialization code.
     */
    @Override
    public void robotInit() {
        // Instantiate our RobotContainer.  This will perform all our button bindings, and put our
        // autonomous chooser on the dashboard.
        m_robotContainer = new RobotContainer();

        Field2d simField = ((SimDrive) m_robotContainer.drivetrain).field;

pathfinder = new Pathfinder(Field.CHARGED_UP_2023);

try {
    Path testPath = pathfinder.generatePath(new Vertex(1, 1), new Vertex(8, 4));
} catch (ImpossiblePathException e){
    e.printStackTrace();
}

        ArrayList<ArrayList<Pose2d>> listList = pathfinder.visualizeField();
        for(int i = 0; i < listList.size(); i++){
            simField.getObject("Obstacle" + i).setPoses(listList.get(i));
        }
        SmartDashboard.putNumberArray("Start Vertex", lastStart);
        SmartDashboard.putNumberArray("End Vertex", lastEnd);
    }

        

    /**
     * This function is called every 20 ms, no matter the mode. Use this for items like diagnostics
     * that you want ran during disabled, autonomous, teleoperated and test.
     *
     * <p>This runs after the mode specific periodic functions, but before LiveWindow and
     * SmartDashboard integrated updating.
     */
    @Override
    public void robotPeriodic() {
        // Runs the Scheduler.  This is responsible for polling buttons, adding newly-scheduled
        // commands, running already-scheduled commands, removing finished or interrupted commands,
        // and running subsystem periodic() methods.  This must be called from the robot's periodic
        // block in order for anything in the Command-based framework to work.
        CommandScheduler.getInstance().run();
        SmartDashboard.putData(CommandScheduler.getInstance());

        if(!SmartDashboard.getNumberArray("Start Vertex", lastStart).equals(lastStart) || !SmartDashboard.getNumberArray("End Vertex", lastEnd).equals(lastEnd)){
            lastStart = SmartDashboard.getNumberArray("Start Vertex", lastStart);
            lastEnd = SmartDashboard.getNumberArray("End Vertex", lastEnd);
            try {
                Path testPath = pathfinder.generatePath(new Vertex(lastStart[0], lastStart[1]), new Vertex(lastEnd[0], lastEnd[1]), 
                    PathfindSnapMode.SNAP_ALL_THEN_LINE);
                ((SimDrive) m_robotContainer.drivetrain).field.getObject("Path").setPoses(testPath.asPose2dList());
            } catch (ImpossiblePathException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * This function is called once each time the robot enters Disabled mode.
     */
    @Override
    public void disabledInit() {
    }

    @Override
    public void disabledPeriodic() {
    }

    /**
     * This autonomous runs the autonomous command selected by your {@link RobotContainer} class.
     */
    @Override
    public void autonomousInit() {
        m_autonomousCommand = m_robotContainer.getAutonomousCommand();

        // schedule the autonomous command (example)
        if (m_autonomousCommand != null) {
            m_autonomousCommand.schedule();
        }
    }

    /**
     * This function is called periodically during autonomous.
     */
    @Override
    public void autonomousPeriodic() {
    }

    @Override
    public void teleopInit() {
        // This makes sure that the autonomous stops running when
        // teleop starts running. If you want the autonomous to
        // continue until interrupted by another command, remove
        // this line or comment it out.
        if (m_autonomousCommand != null) {
            m_autonomousCommand.cancel();
        }

        CommandScheduler.getInstance().schedule(RobotContainer.driveCommand);
    }

    /**
     * This function is called periodically during operator control.
     */
    @Override
    public void teleopPeriodic() {
    }

    @Override
    public void testInit() {
        // Cancels all running commands at the start of test mode.
        CommandScheduler.getInstance().cancelAll();
    }

    /**
     * This function is called periodically during test mode.
     */
    @Override
    public void testPeriodic() {
    }

    /**
     * This function is called once when the robot is first started up.
     */
    @Override
    public void simulationInit() {
    }

    /**
     * This function is called periodically whilst in simulation.
     */
    @Override
    public void simulationPeriodic() {
    }
}
