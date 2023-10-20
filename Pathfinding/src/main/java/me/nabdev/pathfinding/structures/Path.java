package me.nabdev.pathfinding.structures;

import java.util.ArrayList;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.math.trajectory.TrajectoryGenerator;
import me.nabdev.pathfinding.Pathfinder;
import me.nabdev.pathfinding.Pathfinder.PathfindSnapMode;

/**
 * A Path is a list of Vertices that represent a path from a start point to a
 * target point.
 */
@SuppressWarnings("deprecation")
public class Path extends ArrayList<Vertex> {
    /**
     * The start vertex of the path.
     */
    private Vertex start;
    /**
     * The target vertex of the path.
     */
    private Vertex target;

    /**
     * The full path including the start and target vertices.
     */
    private ArrayList<Vertex> fullPath = new ArrayList<Vertex>();
    /**
     * The segments of the path.
     */
    private ArrayList<PathSegment> segments = new ArrayList<PathSegment>();

    /**
     * Represents the target vertex before it was snapped to be outside of all
     * obstacles.
     */
    private Vertex unsnappedTarget = null;
    /**
     * The Pathfinder that created this path.
     */
    private Pathfinder pathfinder;

    /**
     * The snap mode used to create this path.
     */
    public PathfindSnapMode snapMode = PathfindSnapMode.SNAP_ALL;

    /**
     * Creates a new Path with the given start and target vertices.
     * 
     * @param start      The start vertex of the path.
     * @param target     The target vertex of the path.
     * @param pathfinder The Pathfinder that created this path.
     */
    public Path(Vertex start, Vertex target, Pathfinder pathfinder) {
        super();
        this.start = start;
        this.target = target;
        this.pathfinder = pathfinder;
    }

    public Path(Vertex start, Vertex target, ArrayList<Vertex> points) {
        super();
        this.start = start;
        this.target = target;
        this.addAll(points);

        createFullPath();
    }

    /**
     * Set the original unsnapped target vertex (the target vertex before it was
     * snapped to be outside of all obstacles).
     * 
     * @param unsnappedTarget The original unsnapped target vertex.
     */
    public void setUnsnappedTarget(Vertex unsnappedTarget) {
        this.unsnappedTarget = unsnappedTarget;
    }

    /**
     * Get the unsnapped target vertex.
     * 
     * @return The unsnapped target vertex
     */
    public Vertex getUnsnappedTarget() {
        return unsnappedTarget;
    }

    /**
     * Get the last vertex in the path (not including the target vertex)
     * 
     * @return The last vertex in the path.
     */
    public Vertex end() {
        return this.size() > 0 ? this.get(this.size() - 1) : this.start;
    }

    /**
     * Apply all processing to the path to prepare it for use.
     * 
     * @param snapMode The snap mode to use.
     */
    public void processPath(PathfindSnapMode snapMode) {
        this.snapMode = snapMode;
        createFullPath();
        bezierSmoothing();
        addFinalSegment(snapMode);
        if (pathfinder.injectPoints)
            injectPoints();
        updateFromSegments();
    }

    // This probably needs to change in the future.
    @Deprecated
    private void addFinalSegment(PathfindSnapMode snapMode) {
        if (snapMode == PathfindSnapMode.SNAP_ALL_THEN_LINE || snapMode == PathfindSnapMode.SNAP_TARGET_THEN_LINE) {
            PathSegment seg = new PathSegment(target, unsnappedTarget);
            segments.add(seg);
        }
    }

    private void bezierSmoothing() {
        // "this" does not include the start and endpoint, so in the case where the
        // shortest path is a straight line it would be empty.
        if (this.size() < 1) {
            segments.add(new PathSegment(start, target));
            return;
        }
        // Iterate over every vertex other than the start and end.
        for (int i = 0; i < this.size(); i++) {
            Vertex p1 = this.get(i);
            PathSegment curve = new PathSegment();
            Vertex prev = fullPath.get(i);
            // fullPath takes into account the start and endpoint while this does not, so we
            // can garuntee that i + 2 will never be out of bounds.
            Vertex next = fullPath.get(i + 2);

            double cornerDist = pathfinder.cornerDist;
            // We are creating vectors from the current point to the previous and next
            // points to find the other two control points for our quadratic bezier curve.
            Vector prevVector = prev.createVectorFrom(p1);
            double prevMag = prevVector.magnitude();
            if (prevMag < pathfinder.cornerDist * 2) {
                if (i > 0) {
                    prevVector = prevVector.scale(pathfinder.cornerSplitPercent);
                    cornerDist = prevMag * pathfinder.cornerSplitPercent;
                } else {
                    prevVector = prevVector.normalize().scale(Math.min(pathfinder.cornerDist, prevMag));
                    cornerDist = Math.min(pathfinder.cornerDist, prevMag);
                }
            } else {
                prevVector = prevVector.normalize().scale(pathfinder.cornerDist);
            }

            Vector nextVector = next.createVectorFrom(p1);
            double nextMag = nextVector.magnitude();
            if (nextMag < pathfinder.cornerDist * 2) {
                if (i < this.size() - 1) {
                    nextVector = nextVector.scale(pathfinder.cornerSplitPercent);
                    cornerDist += nextMag * pathfinder.cornerSplitPercent;
                } else {
                    nextVector = nextVector.normalize().scale(Math.min(pathfinder.cornerDist, nextMag));
                    cornerDist += Math.min(pathfinder.cornerDist, nextMag);
                }
            } else {
                nextVector = nextVector.normalize().scale(pathfinder.cornerDist);
                cornerDist += pathfinder.cornerDist;
            }

            // These are the two points that will be used as control points for the bezier
            // curve.
            Vertex p0 = p1.moveByVector(prevVector);
            Vertex p2 = p1.moveByVector(nextVector);

            // This is the actual bezier curve.
            generateBezierCorner(curve, cornerDist * 0.5, p0, p1, p2);

            // Just connecting the dots.
            if (i == 0) {
                segments.add(new PathSegment(start, curve.start()));
            } else {
                segments.add(new PathSegment(segments.get(2 * i - 1).end(), curve.start()));
            }
            segments.add(curve);
        }
        // Finally, add a segment between the point before the target and the target.
        segments.add(new PathSegment(segments.get(segments.size() - 1).end(), target));
    }

    /**
     * Generates a bezier curve between the three given points.
     * 
     * @param curve The PathSegment to add the points to.
     * @param p0    The first point.
     * @param p1    The second point (the point that won't lie on the curve).
     * @param p2    The third point.
     */
    private void generateBezierCorner(PathSegment curve, double cornerDist, Vertex p0, Vertex p1, Vertex p2) {
        // Want to understand how this function works?
        // I highly recomend checking out the visualization at
        // https://en.wikipedia.org/wiki/B%C3%A9zier_curve#Quadratic_curves
        // It's a lot easier to understand when you can see it.
        double realCornerDist = pathfinder.normalizeCorners ? cornerDist : 1;
        for (double t = 0; t < realCornerDist; t += pathfinder.cornerPointSpacing) {
            Vector v0 = p1.createVectorFrom(p0);
            if (pathfinder.normalizeCorners)
                v0 = v0.normalize();
            Vertex q0 = p0.moveByVector(v0.scale(t));

            Vector v1 = p2.createVectorFrom(p1);
            if (pathfinder.normalizeCorners)
                v1 = v1.normalize();
            Vertex q1 = p1.moveByVector(v1.scale(t));

            Vector v2 = q1.createVectorFrom(q0);
            if (pathfinder.normalizeCorners)
                v2 = v2.normalize();
            Vertex pos = q0.moveByVector(v2.scale(t));

            curve.add(pos);
        }
    }

    // Just add all the points from the segments to the path in order.
    private void updateFromSegments() {
        this.clear();
        for (PathSegment seg : segments) {
            for (int i = 0; i < seg.points.size(); i++) {
                if (!this.contains(seg.get(i))) // Weeding out potential duplicates
                    this.add(seg.get(i));
            }
        }
        // "this" shouldn't contain the start and endpoint, so we remove them here.
        if (this.size() > 0)
            this.remove(0);
        if (this.size() > 0)
            this.remove(this.size() - 1);
    }

    // Create the full path including the start and endpoint.
    private void createFullPath() {
        fullPath.clear();
        fullPath.add(start);
        fullPath.addAll(this);
        fullPath.add(target);
    }

    // Adding points in the middle of straight segments to allow for pure pursuit to
    // work its magic.
    private void injectPoints() {
        for (int x = 0; x < segments.size(); x++) {
            ArrayList<Vertex> newPoints = new ArrayList<Vertex>();
            PathSegment seg = segments.get(x);
            if (seg.corner)
                continue;
            Vertex startPoint = seg.get(0);
            Vertex endPoint = seg.get(1);

            double dx = endPoint.x - startPoint.x;
            double dy = endPoint.y - startPoint.y;
            double length = Math.sqrt(dx * dx + dy * dy);
            double numPoints = Math.round(length / pathfinder.pointSpacing);
            double stepX = dx / numPoints;
            double stepY = dy / numPoints;

            for (int i = 0; i < numPoints; i++) {
                double newX = startPoint.x + stepX * i;
                double newY = startPoint.y + stepY * i;
                newPoints.add(new Vertex(newX, newY));
            }
            newPoints.add(endPoint);
            seg.replace(newPoints);
        }
    }

    /**
     * Get rotation at the final point of the path
     * 
     * @return The final rotation of the path.
     */
    public Rotation2d getFinalRot() {
        Vertex end = end();
        if (end == null)
            return null;
        return new Rotation2d(Math.atan2(target.y - end.y, target.x - end.x));
    }

    /**
     * Get the path as a double array [x, y, rotation, x, y, rotation, ...].
     * Rotation is in degrees.
     * 
     * @return The path as a double array.
     */
    public double[] toDoubleArray() {
        ArrayList<Double> temp = new ArrayList<Double>();
        temp.add(start.x);
        temp.add(start.y);
        temp.add(start.rotation.getDegrees());
        for (Vertex v : this) {
            temp.add(v.x);
            temp.add(v.y);
            temp.add(v.rotation.getDegrees());
        }
        if (snapMode == PathfindSnapMode.SNAP_ALL_THEN_LINE || snapMode == PathfindSnapMode.SNAP_TARGET_THEN_LINE) {
            temp.add(unsnappedTarget.x);
            temp.add(unsnappedTarget.y);
            temp.add(unsnappedTarget.rotation.getDegrees());
        } else {
            temp.add(target.x);
            temp.add(target.y);
            temp.add(target.rotation.getDegrees());
        }
        double[] finalArr = new double[temp.size()];
        for (int i = 0; i < temp.size(); i++) {
            finalArr[i] = temp.get(i);
        }
        return finalArr;
    }

    /**
     * Get the path as a Pose2d ArrayList.
     * 
     * @return An ArrayList of Pose2ds representing the path.
     */
    public ArrayList<Pose2d> asPose2dList() {
        ArrayList<Pose2d> poses = new ArrayList<Pose2d>();
        poses.add(start.asPose2d());
        for (Vertex v : this) {
            // Calculate the heading from this point to the next point
            Rotation2d rot = new Rotation2d(Math.atan2(v.y - poses.get(poses.size() - 1).getY(),
                    v.x - poses.get(poses.size() - 1).getX()));
            poses.add(new Pose2d(v.x, v.y, rot));
        }
        if (snapMode == PathfindSnapMode.SNAP_ALL_THEN_LINE || snapMode == PathfindSnapMode.SNAP_TARGET_THEN_LINE) {
            Rotation2d rot = new Rotation2d(Math.atan2(unsnappedTarget.y - poses.get(poses.size() - 1).getY(),
                    unsnappedTarget.x - poses.get(poses.size() - 1).getX()));
            poses.add(new Pose2d(unsnappedTarget.x, unsnappedTarget.y, rot));
        } else {
            poses.add(new Pose2d(target.x, target.y, getFinalRot()));
        }
        return poses;
    }

    /**
     * Get the path as a Trajectory.
     * 
     * @param config The TrajectoryConfig to use.
     * @return The path as a Trajectory.
     */
    public Trajectory asTrajectory(TrajectoryConfig config) {
        return TrajectoryGenerator.generateTrajectory(asPose2dList(), config);
    }

    /**
     * Get the start vertex.
     * 
     * @return The start vertex
     */
    public Vertex getStart() {
        return start;
    }

    /**
     * Get the target vertex.
     * 
     * @return The target vertex
     */
    public Vertex getTarget() {
        return target;
    }
}