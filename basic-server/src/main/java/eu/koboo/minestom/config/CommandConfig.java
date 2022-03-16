package eu.koboo.minestom.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CommandConfig {

    private final List<String> commandNames;

    public static CommandConfig load() {
        String filePath = "configs/commands.yml";
        List<String> content = new ArrayList<>();
        try {
            File file = new File(filePath);
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    if(line.startsWith("#")) {
                        continue;
                    }
                    content.add(line.toLowerCase(Locale.ROOT));
                }
                reader.close();
            } else {
                file.createNewFile();
                PrintWriter writer = new PrintWriter(filePath, StandardCharsets.UTF_8);
                content.add("fly");
                content.add("flyspeed");
                content.add("gamemode");
                content.add("spawn");
                content.add("spectate");
                content.add("stop");
                content.add("teleport");
                content.add("teleporthere");
                writer.println("#These are the command provided by default.");
                writer.println("#If you want to disable them, just remove them.");
                for (String cmd : content) {
                    writer.println(cmd);
                }
                writer.close();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Error while loading CommandConfig: ", e);
        }
        return new CommandConfig(content);
    }

}
