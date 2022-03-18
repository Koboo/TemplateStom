package eu.koboo.minestom.console;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandResult;
import net.minestom.server.command.builder.CommandResult.Type;
import org.jline.reader.Candidate;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReader.Option;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class Console {

    private static final String CONSOLE_NAME = "basicstom-console";

    private final Thread consoleThread;
    private final Terminal terminal;
    private final LineReader lineReader;

    public Console() {
        try {
            terminal = TerminalBuilder.builder()
                    .system(true)
                    .name(CONSOLE_NAME)
                    .build();
        } catch (IOException e) {
            throw new IllegalStateException("Terminal could not be created.", e);
        }

        lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .appName(CONSOLE_NAME)
                .completer((reader, line, candidates) -> {
                    Set<Command> commands = new HashSet<>(
                            MinecraftServer.getCommandManager().getDispatcher().getCommands());

                    commands.removeIf(command -> !line.line().contains(command.getName()) || Arrays.stream(
                                    command.getAliases()).filter(Objects::nonNull)
                            .noneMatch(alias -> line.line().contains(alias)));

                    for (Command command : commands) {
                        candidates.add(new Candidate(command.getName()));
                        for (String alias : command.getAliases()) {
                            if (alias == null) {
                                continue;
                            }
                            if (line.line().contains(alias)) {
                                candidates.add(new Candidate(alias));
                            }
                        }
                    }
                })
                .build();
        lineReader.setOpt(Option.DISABLE_EVENT_EXPANSION);
        lineReader.unsetOpt(Option.INSERT_TAB);

        consoleThread = new Thread(() -> {
            try {
                String line;
                while (!MinecraftServer.isStopping()) {
                    try {
                        line = lineReader.readLine();
                    } catch (EndOfFileException eofe) {
                        continue;
                    }
                    String cmd = line.trim();
                    if (!cmd.isEmpty()) {
                        CommandResult result = MinecraftServer.getCommandManager()
                                .execute(MinecraftServer.getCommandManager().getConsoleSender(), cmd);
                        if(result.getType() != Type.SUCCESS || result.getType() != Type.INVALID_SYNTAX) {
                            if(result.getType() == Type.CANCELLED) {
                                Audiences.console().sendMessage(Component.text("The command '" + cmd + "' got cancelled."));
                            }
                            if(result.getType() == Type.UNKNOWN) {
                                Audiences.console().sendMessage(Component.text("The command '" + cmd + "' is unknown."));
                            }
                        }
                    }
                }
            } catch (UserInterruptException e) {
                System.exit(99);
            } finally {
                try {
                    terminal.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        consoleThread.setDaemon(true);
        consoleThread.setName("ConsoleReader");
    }

    public void start() {
        consoleThread.start();
    }

    public void stop() {
        if(consoleThread.isInterrupted()) {
            return;
        }
        consoleThread.interrupt();
    }

}
