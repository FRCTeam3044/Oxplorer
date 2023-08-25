package me.nabdev.pathfinding.structures;

/**
 * An exception that is thrown when a path is impossible to find for various
 * reasons.
 */
public class ImpossiblePathException extends Exception {
    /**
     * Creates a new ImpossiblePathException with the given message.
     * 
     * @param message The message to be displayed when the exception is thrown.
     */
    public ImpossiblePathException(String message) {
        super(message);
    }
}