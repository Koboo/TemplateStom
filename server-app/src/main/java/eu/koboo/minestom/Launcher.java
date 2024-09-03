package eu.koboo.minestom;

import eu.koboo.minestom.server.ServerImpl;
import org.fusesource.jansi.AnsiConsole;

public class Launcher {

    public static void main(String[] args) {
        try {
            AnsiConsole.systemInstall();
            new ServerImpl(args);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
