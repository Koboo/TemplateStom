package eu.koboo.minestom.console;

import eu.koboo.minestom.server.CurrentBuild;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandResult;
import org.jline.reader.*;
import org.jline.reader.LineReader.Option;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class JLineConsole {

    LineReader lineReader;

    public JLineConsole() {
        String consoleName = CurrentBuild.NAME + "-console";

        Terminal terminal;
        try {
            terminal = TerminalBuilder.builder()
                .system(true)
                .name(consoleName)
                .build();
        } catch (IOException e) {
            throw new IllegalStateException("Terminal could not be created.", e);
        }

        lineReader = LineReaderBuilder.builder()
            .terminal(terminal)
            .appName(consoleName)
            .completer((reader, line, candidates) -> {
                Set<Command> commands = new HashSet<>(
                    MinecraftServer.getCommandManager().getDispatcher().getCommands());

                commands.removeIf(command ->
                    !line.line().contains(command.getName())
                        || Arrays.stream(command.getAliases()).filter(Objects::nonNull)
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
    }

    public void start() {

        Thread consoleThread = new Thread(() -> {
            log.info("Ready to execute console commands!");
            try {
                String line;
                while (!MinecraftServer.isStopping()) {
                    try {
                        line = lineReader.readLine();
                    } catch (EndOfFileException eofException) {
                        continue;
                    }
                    String commandInput = line.trim();
                    if (commandInput.isEmpty()) {
                        continue;
                    }
                    ConsoleSender consoleSender = MinecraftServer.getCommandManager().getConsoleSender();
                    CommandResult result = MinecraftServer.getCommandManager().execute(consoleSender, commandInput);
                    Audience console = Audiences.console();
                    console.sendMessage(Component.text("The executed command resulted in \"" + result.getType().name() + "\"."));
                }
            } catch (UserInterruptException e) {
                throw new RuntimeException("User interrupted: ", e);
            }
        });
        consoleThread.setDaemon(true);
        consoleThread.setName("ConsoleCommandInputThread");
        consoleThread.start();
    }
}
