package connectorClient;

import channel.commandPattern.CommandFactory;

public class UpdateThread implements Runnable {
    private CommandFactory commandFactory;
    private boolean stopped;

    public UpdateThread(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
        this.stopped = false;
    }

    public void run() {
        while (!stopped) {
            try {
                Thread.sleep(300);
                for (String command : commandFactory.listCommands()) {
                    commandFactory.executeCommand(command);
                }
            } catch (InterruptedException e) {
                System.out.println("Closing GUI");
                return;
            }
        }
    }

    public void stop() {
        this.stopped = true;
    }
}
