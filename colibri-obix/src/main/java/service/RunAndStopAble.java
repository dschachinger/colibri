package service;

public interface RunAndStopAble extends Runnable {

    /**
     * Stops the execution of the run().
     */
    public void stop();
}
