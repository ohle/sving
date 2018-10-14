package de.eudaemon.util;

/**
 * Generic {@link RuntimeException} that should be thrown whenever an {@link Exception}
 * was caught under unanticipated (thought to be impossible) circumstances
 */
public class UnanticipatedException
        extends RuntimeException {

    public UnanticipatedException(Exception cause) {
        super(cause);
    }

    public UnanticipatedException(String message, Exception cause) {
        super(message, cause);
    }
}
