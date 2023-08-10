package me.nabdev.pathfinding.Structures;

import java.util.ArrayList;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import me.nabdev.pathfinding.Pathfinder;

/**
 * A Path is a list of Vertices that represent a path from a start point to a
 * target point.
 */
public class Path extends ArrayList<Vertex> {
    /**
     * The start vertex of the path.
     */
    public Vertex start;
    /**
     * The target vertex of the path.
     */
    public Vertex target;

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
     * Get the last vertex in the path (not including the target vertex)
     * 
     * @return The last vertex in the path.
     */
    public Vertex end() {
        return this.size() > 0 ? this.get(this.size() - 1) : this.start;
    }

    /**
     * Apply all processing to the path to prepare it for use.
     */
    public void processPath() {
        createFullPath();
        bezierSmoothing();
        addFinalSegment();
        injectPoints();
        updateFromSegments();
    }

    // This probably needs to change in the future.
    private void addFinalSegment() {
        if (unsnappedTarget == null)
            return;
        PathSegment seg = new PathSegment(target, unsnappedTarget);
        segments.add(seg);
    }

    private void bezierSmoothing() {
        // this does not include the start and endpoint, so in the case where the
        // shortest path is a straight line it would be empty.
        if (this.size() < 1) {
            segments.add(new PathSegment(start, target));
            return;
        }
        for (int i = 0; i < this.size(); i++) {
            Vertex p1 = this.get(i);
            PathSegment curve = new PathSegment();
            Vertex prev = fullPath.get(i);
            // fullPath takes into account the start and endpoint while this does not, so we
            // can garuntee that i + 2 will never be out of bounds.
            Vertex next = fullPath.get(i + 2);

            Vector prevVector = prev.createVectorFrom(p1).normalize().scale(pathfinder.cornerDist);
            Vector nextVector = next.createVectorFrom(p1).normalize().scale(pathfinder.cornerDist);

            Vertex p0 = p1.moveByVector(prevVector);
            Vertex p2 = p1.moveByVector(nextVector);

            generateBezierCorner(curve, p0, p1, p2);

            if (i == 0) {
                segments.add(new PathSegment(start, curve.start()));
            } else {
                segments.add(new PathSegment(segments.get(2 * i - 1).end(), curve.start()));
            }
            segments.add(curve);
        }
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
    private void generateBezierCorner(PathSegment curve, Vertex p0, Vertex p1, Vertex p2) {
        for (double t = 0; t < pathfinder.cornerDist; t += pathfinder.smoothSpacing) {
            Vertex q0 = p0.moveByVector(p1.createVectorFrom(p0).normalize().scale(t));
            Vertex q1 = p1.moveByVector(p2.createVectorFrom(p1).normalize().scale(t));
            Vertex pos = q0.moveByVector(q1.createVectorFrom(q0).normalize().scale(t));
            curve.add(pos);
        }
    }

    private void updateFromSegments() {
        this.clear();
        for (PathSegment seg : segments) {
            for (int i = 0; i < seg.points.size(); i++) {
                if (!this.contains(seg.get(i)))
                    this.add(seg.get(i));
            }
        }
        if (this.size() > 0)
            this.remove(0);
        if (this.size() > 0)
            this.remove(this.size() - 1);
    }

    private void createFullPath() {
        fullPath.clear();
        fullPath.add(start);
        fullPath.addAll(this);
        fullPath.add(target);
    }

    private void injectPoints() {
        ArrayList<Vertex> newPoints = new ArrayList<Vertex>();
        // Create an ArrayList of Edges from the path
        for (int x = 0; x < segments.size(); x++) {
            PathSegment seg = segments.get(x);
            if (seg.corner)
                continue;
            Vertex startPoint = seg.get(0);
            Vertex endPoint = seg.get(1);
            Vector vector = endPoint.createVectorFrom(startPoint);
            double mag = vector.magnitude();
            double numPoints = Math.round(mag / pathfinder.pointSpacing);
            vector = vector.normalize().scale(pathfinder.pointSpacing);
            for (int i = 0; i < numPoints; i++) {
                newPoints.add(startPoint.moveByVector(vector.scale(i)));
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
        if (unsnappedTarget != null) {
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
            poses.add(v.asPose2d());
        }
        if (unsnappedTarget != null) {
            poses.add(unsnappedTarget.asPose2d());
        } else {
            poses.add(target.asPose2d());
        }
        return poses;
    }
}