package service.commandPattern;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * This class represents a command factory, which is used in the command pattern.
 */
public class CommandFactory {

    /******************************************************************
     *                            Variables                           *
     ******************************************************************/

    /**
     * Map with command-ID as keys and {@link Command} as values.
     */
    private final Map<String, Command> commands;

    /******************************************************************
     *                            Constructors                        *
     ******************************************************************/

    public CommandFactory() {
        this.commands = new HashMap<>();
    }

    /******************************************************************
     *                            Methods                             *
     ******************************************************************/

    public void executeCommand(String name) {
        if(commands.containsKey(name)) {
            commands.get(name).apply();
        }
    }

    /******************************************************************
     *                      Getter and Setter                         *
     ******************************************************************/

    public void addCommand(String name, Command command) {
        commands.put(name, command);
    }

    public void addCommand(Command command) {
        commands.put(UUID.randomUUID().toString(), command);
    }

    public void removeCommand(String name) {
        commands.remove(name);
    }

    public Set<String> listCommands() {
        return commands.keySet();
    }
}
