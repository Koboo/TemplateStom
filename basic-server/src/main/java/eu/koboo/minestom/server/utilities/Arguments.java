package eu.koboo.minestom.server.utilities;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;

public class Arguments {

    public static final ArgumentEntity TARGET = ArgumentType.Entity("target").onlyPlayers(true).singleEntity(true);


}
