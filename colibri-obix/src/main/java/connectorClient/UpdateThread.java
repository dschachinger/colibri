package connectorClient;

import service.commandPattern.CommandFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.RunAndStopAble;

/**
 * This class is used to update the GUI frequently after a few milliseconds.
 * All commands which are stored in a command factory are executed therefore.
 */
public class UpdateThread implements RunAndStopAble {

    /******************************************************************
     *                            Variables                           *
     ******************************************************************/

    private CommandFactory commandFactory;
    private boolean stopped;
    private static final Logger logger = LoggerFactory.getLogger(UpdateThread.class);

    /******************************************************************
     *                            Constructors                        *
     ******************************************************************/

    public UpdateThread(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
        this.stopped = false;
    }

    /******************************************************************
     *                            Methods                             *
     ******************************************************************/

    public void run() {
        while (!stopped) {
            try {
                /**
                 * Every 300 milliseconds, all commands stored in the {@link #commandFactory} are executed.
                 * The {@link #commandFactory} contains commands to update the GUI of the OBIX connector and
                 * the {@link model.obix.ObixObject} presented in the GUI.
                 */
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
