package eu.koboo.minestom.console;

import eu.koboo.minestom.server.Server;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import org.fusesource.jansi.Ansi;
import org.tinylog.Level;
import org.tinylog.core.LogEntry;
import org.tinylog.core.LogEntryValue;
import org.tinylog.writers.Writer;

public class ServerConsoleWriter implements Writer {

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    public ServerConsoleWriter(Map<String, String> properties) { }

    @Override
    public Collection<LogEntryValue> getRequiredLogEntryValues() {
        return EnumSet.of(
                LogEntryValue.DATE, LogEntryValue.LEVEL, LogEntryValue.THREAD, LogEntryValue.CLASS,
                LogEntryValue.MESSAGE
        );
    }

    @Override
    public void write(LogEntry logEntry) throws Exception {
        if (logEntry.getLevel().ordinal() < Level.INFO.ordinal()) {
            return;
        }
        String time = TIME_FORMAT.format(logEntry.getTimestamp().toDate());
        String[] packagePath = logEntry.getClassName().split("\\.");
        String clazz = packagePath[packagePath.length - 1];

        Ansi ansi = Ansi.ansi();

        ansi = ansi.fgCyan().a("[" + time + "] ").reset();
        ansi = ansi.a("[" + logEntry.getThread() + "] ").reset();
        ansi = ansi.fgYellow().a("[" + logEntry.getLevel().name() + "] ").reset();
        ansi = ansi.fgMagenta().a("[" + clazz + "] ").reset();
        ansi = appendWithColor(ansi, logEntry.getLevel(), logEntry.getMessage());

        System.out.println(ansi);
    }

    @Override
    public void flush() throws Exception {
        System.out.flush();
    }

    @Override
    public void close() throws Exception {
        Server.getInstance().getConsole().stop();
    }

    private Ansi appendWithColor(Ansi ansi, Level level, String message) {
        switch (level) {
            case INFO, OFF -> ansi = ansi.a(message).reset();
            case WARN -> ansi = ansi.fgBrightYellow().a(message).reset();
            case ERROR -> ansi = ansi.fgRed().a(message).reset();
            case DEBUG -> ansi = ansi.fgBrightCyan().a(message).reset();
            case TRACE -> ansi = ansi.fgBrightBlue().a(message).reset();
        }
        return ansi;
    }
}
