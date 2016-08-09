package connectorClient;

import service.commandPattern.CommandFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.RunAndStopAble;

public class UpdateThread implements RunAndStopAble {
    private CommandFactory commandFactory;
    private boolean stopped;
    private static final Logger logger = LoggerFactory.getLogger(UpdateThread.class);

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
                logger.info("Closing GUI");
                return;
            }
        }
    }

    public void stop() {
        this.stopped = true;
    }
}
