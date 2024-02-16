package me.nabdev.pathfinding.structures;

import java.util.ArrayList;

/**
 * Represents a group of obstacles, which can be used as a janky way to
 * represent concave obstacles.
 */
public class ObstacleGroup {
    private ArrayList<Obstacle> obstacles;

    /**
     * Creates a new obstacle group.
     * @param obstacles The obstacles in the group.
     */
    public ObstacleGroup(Obstacle... obstacles) {
        this.obstacles = new ArrayList<Obstacle>();
        for (Obstacle obstacle : obstacles) {
            this.obstacles.add(obstacle);
        }
    }

    /**
     * Checks if a vertex is inside any of the obstacles in the group.
     * 
     * @param pos The vertex to check.
     * @return True if the vertex is inside any of the obstacles, false otherwise.
     */
    public boolean isInside(Vertex pos) {
        for (Obstacle obstacle : obstacles) {
            if (obstacle.isInside(pos)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the closest point on the obstacles to the given vertex.
     * 
     * @param pos The vertex to calculate the nearest point to.
     * @return The nearest point on the obstacles to the given vertex.
     */
    public Vertex getClosestPoint(Vertex pos) {
        Vertex closest = null;
        double minDistance = Double.MAX_VALUE;
        for (Obstacle obstacle : obstacles) {
            Vertex nearest = obstacle.calculateNearestPoint(pos);
            double distance = pos.distance(nearest);
            if (distance < minDistance) {
                minDistance = distance;
                closest = nearest;
            }
        }
        return closest;
    }
}
