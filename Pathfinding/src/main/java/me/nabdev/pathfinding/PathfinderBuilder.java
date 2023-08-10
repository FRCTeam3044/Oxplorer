package me.nabdev.pathfinding;

import me.nabdev.pathfinding.FieldLoader.Field;

/**
 * Builder class for {@link Pathfinder}
 */
public class PathfinderBuilder {
    private Field field;
    private double pointSpacing = 0.12;
    private double smoothSpacing = 0.08;
    private double cornerDist = 0.6;
    private double clearance = 0.6;

    /**
     * Creates a new PathfinderBuilder with the given {@link Field}
     * 
     * @param field The {@link Field} to use
     */
    public PathfinderBuilder(Field field) {
        this.field = field;
    }

    /**
     * Sets the point spacing (space between injected points on straightaways when
     * generating paths)
     * 
     * @param pointSpacing The point spacing, default 0.12 (meters)
     * @return The builder
     */
    public PathfinderBuilder setPointSpacing(double pointSpacing) {
        this.pointSpacing = pointSpacing;
        return this;
    }

    /**
     * Sets the smooth spacing (space between points on curves when generating
     * paths)
     * 
     * @param smoothSpacing The smooth spacing, default 0.08 (percent of the curve
     *                      length)
     * @return The builder
     */
    public PathfinderBuilder setSmoothSpacing(double smoothSpacing) {
        this.smoothSpacing = smoothSpacing;
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
        this.cornerDist = cornerDist;
        return this;
    }

    /**
     * Sets the clearance (how far away from the obstacles to stay).
     * Usually the radius of your robots circumcircle.
     * 
     * @param clearance The clearance, default 0.6 (meters)
     * @return The builder
     */
    public PathfinderBuilder setClearance(double clearance) {
        this.clearance = clearance;
        return this;
    }

    /**
     * Builds the {@link Pathfinder}
     * 
     * @return The {@link Pathfinder}
     */
    public Pathfinder build() {
        return new Pathfinder(field, pointSpacing, smoothSpacing, cornerDist, clearance);
    }
}
