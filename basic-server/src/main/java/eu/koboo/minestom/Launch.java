package eu.koboo.minestom;

import eu.koboo.minestom.server.Server;
import org.fusesource.jansi.AnsiConsole;

public class Launch {

    public static void main(String[] args) {
        try {
            AnsiConsole.systemInstall();
            new Server();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
