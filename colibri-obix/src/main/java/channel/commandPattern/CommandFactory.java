package channel.commandPattern;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CommandFactory {
    private final Map<String, Command> commands;

    public CommandFactory() {
        this.commands = new HashMap<>();
    }

    public void addCommand(String name, Command command) {
        commands.put(name, command);
    }
    public void addCommand(Command command) {
        commands.put(UUID.randomUUID().toString(), command);
    }

    public void executeCommand(String name) {
        if(commands.containsKey(name)) {
            commands.get(name).apply();
        }
    }

    public Map<String, Command> getCommands() {
        return commands;
    }

    public Set<String> listCommands() {
        return commands.keySet();
    }
}
