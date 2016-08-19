package service;

/**
 * Extends {@link Runnable} with a {@link #stop()} method.
 */
public interface RunAndStopAble extends Runnable {

    /**
     * Stops the execution of the run().
     */
    void stop();
}
