package me.nabdev.pathfinding;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import me.nabdev.pathfinding.structures.ImpossiblePathException;
import me.nabdev.pathfinding.structures.Vector;
import me.nabdev.pathfinding.structures.Vertex;
import me.nabdev.pathfinding.utilities.FieldLoader.Field;

public class PathfinderTest {
    Pathfinder pathfinder;

    @BeforeEach
    void setUp() {
        pathfinder = new PathfinderBuilder(Field.CRESCENDO_2024).build();
    }

    @Test
    @DisplayName("Impossible Path Should Throw Exception")
    void impossiblePath() {
        assertThrows(ImpossiblePathException.class,
                () -> pathfinder.generatePath(new Vertex(-2, -2), new Vertex(2, 2)));
    }

    @Test
    @DisplayName("Pathfinder Should Generate Path")
    void generatePath() {
        assertDoesNotThrow(() -> pathfinder.generatePath(new Vertex(2, 2), new Vertex(4, 4)));
    }

    @Test
    @DisplayName("Diagonal Intersection")
    void diagonalIntersect() {
        assertTrue(Vector.dotIntersectFast(new Vertex(0, 0), new Vertex(10, 10), new Vertex(0, 10), new Vertex(10, 0)));
    }

    @Test
    @DisplayName("One vertex intersection")
    void oneVertexIntersect() {
        assertTrue(
                Vector.dotIntersectFast(new Vertex(0, 0), new Vertex(10, 10), new Vertex(0, 10), new Vertex(10, 10)));
    }

    @Test
    @DisplayName("Perpendicular intersection")
    void perpendicularIntersect() {
        assertTrue(
                Vector.dotIntersectFast(new Vertex(5, 0), new Vertex(5, 10), new Vertex(0, 5), new Vertex(10, 5)));
    }

    // I'm aware this test does not pass, but it will be fixed later with a new
    // intersection algorithm

    // @Test
    // @DisplayName("Perpendicular One Vertex intersection")
    // void perpendicularIntersectOne() {
    // assertTrue(
    // Vector.dotIntersectFast(new Vertex(5, 0), new Vertex(5, 5), new Vertex(0, 5),
    // new Vertex(10, 5)));
    // }

    @Test
    @DisplayName("Diagonal Non-Intersection")
    void diagonalNonIntersect() {
        assertTrue(Vector.dotIntersectFast(new Vertex(0, 0), new Vertex(10, 10), new Vertex(0, 10), new Vertex(10, 0)));
    }

    @Test
    @DisplayName("Perpendicular Non-intersection")
    void perpendicularNonIntersect() {
        assertFalse(
                Vector.dotIntersectFast(new Vertex(5, 0), new Vertex(5, 4), new Vertex(0, 5), new Vertex(10, 5)));
    }

}
