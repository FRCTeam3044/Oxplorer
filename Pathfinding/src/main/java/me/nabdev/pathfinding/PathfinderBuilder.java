package me.nabdev.pathfinding;

import java.io.FileNotFoundException;

import me.nabdev.pathfinding.algorithms.SearchAlgorithm.SearchAlgorithmType;
import me.nabdev.pathfinding.utilities.FieldLoader;
import me.nabdev.pathfinding.utilities.FieldLoader.CornerCutting;
import me.nabdev.pathfinding.utilities.FieldLoader.Field;
import me.nabdev.pathfinding.utilities.FieldLoader.FieldData;

/**
 * Builder class for {@link Pathfinder}
 */
public class PathfinderBuilder {
    private Field field;
    private String customFieldPath;
    private double pointSpacing = 0.25;
    private double cornerPointSpacing = 0.08;
    private double cornerDist = 0.6;
    private double cornerSplitPercent = 0.45;
    private boolean injectPoints = true;
    private boolean normalizeCorners = true;
    private SearchAlgorithmType searchAlgorithmType = SearchAlgorithmType.ASTAR;
    private double robotWidth = 0.7;
    private double robotLength = 0.7;
    private CornerCutting cornerCutting = CornerCutting.LINE;
    private boolean profiling = false;
    private double endgameTime = 25;

    /**
     * Creates a new PathfinderBuilder with the given {@link Field}
     * 
     * @param field The {@link Field} to use
     */
    public PathfinderBuilder(Field field) {
        this.field = field;
    }

    /**
     * Creates a new PathfinderBuilder with the given field json map. Recommended to
     * store this in the deploy directory and use Filesystem.getDeployDirectory() to
     * get the path so it works on the rio and in sim.
     * 
     * @param field The full path to the field json map to use
     */
    public PathfinderBuilder(String field) {
        this.customFieldPath = field;
    }

    /**
     * Sets the point spacing (space between injected points on straightaways when
     * generating paths)
     * 
     * @param pointSpacing The point spacing, default 0.25 (meters)
     * @return The builder
     */
    public PathfinderBuilder setPointSpacing(double pointSpacing) {
        if (pointSpacing <= 0)
            throw new IllegalArgumentException("Point spacing must be greater than 0");
        this.pointSpacing = pointSpacing;
        return this;
    }

    /**
     * Sets the corner point spacing (space between points on curves when generating
     * paths)
     * 
     * @param cornerPointSpacing The corner point spacing, default 0.08
     * @return The builder
     */
    public PathfinderBuilder setCornerPointSpacing(double cornerPointSpacing) {
        if (cornerPointSpacing <= 0)
            throw new IllegalArgumentException("Corner point spacing must be greater than 0");
        this.cornerPointSpacing = cornerPointSpacing;
        return this;
    }

    /**
     * Sets the corner distance (how far back along the straightaway to dedicate to
     * making corners)
     * 
     * @param cornerDist The corner distance, default 0.6 (meters)
     * @return The builder
     */
    public PathfinderBuilder setCornerDist(double cornerDist) {
        if (cornerDist < 0)
            throw new IllegalArgumentException("Corner distance must be positive");
        this.cornerDist = cornerDist;
        return this;
    }

    /**
     * Sets the robot width in meters (used to calculate the clearance)
     * 
     * @param robotWidth The clearance, default 0.7 (meters)
     * @return The builder
     */
    public PathfinderBuilder setRobotWidth(double robotWidth) {
        if (robotWidth <= 0)
            throw new IllegalArgumentException("Robot width must be greater than 0");
        this.robotWidth = robotWidth;
        return this;
    }

    /**
     * Sets the robot length in meters (used to calculate the clearance)
     * 
     * @param robotLength The clearance, default 0.7 (meters)
     * @return The builder
     */
    public PathfinderBuilder setRobotLength(double robotLength) {
        if (robotWidth <= 0)
            throw new IllegalArgumentException("Robot width must be greater than 0");
        this.robotLength = robotLength;
        return this;
    }

    /**
     * Sets the corner split percent (how far each bezier curve should move towards
     * the other point if the distance is too short to allow both corners the full
     * corner distance)
     * 
     * @param cornerSplitPercent The corner split percent, default 0.45 (max 0.5)
     * @return The builder
     */
    public PathfinderBuilder setCornerSplitPercent(double cornerSplitPercent) {
        if (cornerSplitPercent > 0.5)
            throw new IllegalArgumentException("Corner split percent must be less than or equal to 0.5");
        this.cornerSplitPercent = cornerSplitPercent;
        return this;
    }

    /**
     * Sets whether or not to inject points (add points in the middle of
     * straightaways, useful for certain path following algorithms)
     * 
     * @param injectPoints Whether or not to inject points, default false
     * @return The builder
     */
    public PathfinderBuilder setInjectPoints(boolean injectPoints) {
        this.injectPoints = injectPoints;
        return this;
    }

    /**
     * Sets whether or not to normalize corners (make sure that corner points are
     * spaced evenly). Disabling this can greatly help path generation with a higher
     * corner dist and can remove weird artifacts between corners, but will cause
     * issues with certain path following algorithms.
     * 
     * @param normalizeCorners Whether or not to normalize corners, default true
     * @return The builder
     */
    public PathfinderBuilder setNormalizeCorners(boolean normalizeCorners) {
        this.normalizeCorners = normalizeCorners;
        return this;
    }

    /**
     * Sets the search algorithm type to use
     * 
     * @param searchAlgorithmType The search algorithm type, default ASTAR
     * @return The builder
     */
    public PathfinderBuilder setSearchAlgorithmType(SearchAlgorithmType searchAlgorithmType) {
        if (searchAlgorithmType == null)
            throw new IllegalArgumentException("Search algorithm type cannot be null");
        this.searchAlgorithmType = searchAlgorithmType;
        return this;
    }

    /**
     * Sets the corner cutting type to use (See {@link CornerCutting} for more info)
     * 
     * @param cornerCutting The corner cutting type, default LINE
     * @return The builder
     */
    public PathfinderBuilder setCornerCuttingType(CornerCutting cornerCutting) {
        if (cornerCutting == null)
            throw new IllegalArgumentException("Corner cutting type cannot be null");
        if (cornerCutting == CornerCutting.CURVE)
            throw new UnsupportedOperationException("Curve corner cutting is not yet implemented");
        this.cornerCutting = cornerCutting;
        return this;
    }

    /**
     * Sets whether or not to profile the pathfinder (Output path gen time to
     * smartdashboard)
     * 
     * @param profiling Whether or not to profile the pathfinder, default false
     * @return The builder
     */
    public PathfinderBuilder setProfiling(boolean profiling) {
        this.profiling = profiling;
        return this;
    }

    /**
     * Sets the endgame time (time in seconds when the robot should start to
     * consider endgame obstacles). Note that this uses
     * DriverStation.getMatchTime(), so it is not perfectly accurate, and does not
     * work properly when teleop/auto is manually enabled from the driver station
     * (practice mode works fine)
     * 
     * It is recommended to set this to 0 when testing in teleop.
     * 
     * @param endgameTime The endgame time, default 25 (seconds)
     * @return The builder
     */
    public PathfinderBuilder setEndgameTime(double endgameTime) {
        this.endgameTime = endgameTime;
        return this;
    }

    /**
     * Builds the {@link Pathfinder}
     * 
     * @return The {@link Pathfinder}
     */
    public Pathfinder build() {
        FieldData loadedField;
        if (field != null) {
            loadedField = FieldLoader.loadField(field, cornerCutting);
        } else {
            try {
                loadedField = FieldLoader.loadField(customFieldPath, cornerCutting);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Failed to load field from path " + customFieldPath);
            }
        }

        // clearance is the circumcircle radius of the robot
        double clearance = Math.sqrt(Math.pow(robotWidth, 2) + Math.pow(robotLength, 2)) / 2;
        return new Pathfinder(loadedField, pointSpacing, cornerPointSpacing, cornerDist, clearance, cornerSplitPercent,
                injectPoints, normalizeCorners, searchAlgorithmType, profiling, endgameTime);
    }
}
