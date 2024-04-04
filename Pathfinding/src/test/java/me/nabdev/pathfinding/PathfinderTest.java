package me.nabdev.pathfinding;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import me.nabdev.pathfinding.structures.ImpossiblePathException;
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
}
