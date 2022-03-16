package eu.koboo.minestom.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public record OperatorConfig(List<UUID> operatorIdList) {

    private static final String FILE_PATH = "configs/operators.txt";

    public static List<UUID> read() {
        List<UUID> content = new ArrayList<>();
        try {
            File file = new File(FILE_PATH);
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("#") || !line.contains("-")) {
                        continue;
                    }
                    content.add(UUID.fromString(line));
                }
                reader.close();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error while loading OperatorConfig: ", e);
        }
        return content;
    }

    public static void write(List<UUID> uuidList) {
        try {
            PrintWriter writer = new PrintWriter(FILE_PATH, StandardCharsets.UTF_8);
            writer.println("# This is the list of operators.");
            writer.println("# If you append an UUID into this list, the player doesn't need any permissions.");

            for (UUID uuid : uuidList) {
                writer.println(uuid.toString());
            }

            writer.close();
        } catch (IOException e) {
            throw new IllegalStateException("Error while writing OperatorConfig: ", e);
        }
    }

    @SuppressWarnings("all")
    public static OperatorConfig load() {
        List<UUID> content = new ArrayList<>();
        try {
            File file = new File(FILE_PATH);
            if (file.exists()) {
                content.addAll(read());
            } else {
                file.createNewFile();
                write(new ArrayList<>());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error while loading OperatorConfig: ", e);
        }
        return new OperatorConfig(content);
    }

}
