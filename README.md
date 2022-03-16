# MinestomImpl

Fork of [BasicMinestomServer](https://github.com/Protonull/BasicMinestomServer).

This is a fork of [Protonull's BasicMinestomServer](https://github.com/Protonull/BasicMinestomServer).
Further developed and managed by [Koboo](https://github.com/Koboo). This is to
implement some functions of [Minestom](https://minestom.com) and make
them configurable. Also some of the better known commands will be implemented
directly.

In the distant future an additional API may be added.

## Requirements

These things are required for use:

* Java 17
* Minecraft 1.18.2

## Credits

A big thanks to the authors, for open-sourcing their projects!

* Project [BasicMinestomServer](https://github.com/Protonull/BasicMinestomServer)
* Console [Sabre](https://github.com/Project-Cepi/Sabre)
* Commands [VanillaFeatures](https://github.com/JustDoom/VanillaFeatures)

## Current Features

* Commands:
    * ``op`` - Set other players to operator
    * ``deop`` - Remove other players as operator
    * ``gamemode`` - Change gamemode of yourself or other players
    * ``fly`` - Change fly-mode of yourself or other players
    * ``flyspeed`` - Change fly-speed of yourself or other players
    * ``spawn`` - Teleport yourself or others players to spawn
    * ``spectate`` - Spectate other players
    * ``stop`` - Stops the server
    * ``surface`` - Teleport yourself or other players to surface
    * ``teleport`` - Teleport to other players or player to player
    * ``teleporthere`` - Teleport other players to you
* Async Log Files
* Colors in Console (ColorCode support)

## ToDo

* World Data (Save/Load - with Slime or Anvil)
* Operator feature (``/operator``)
* Extension Management through command?
* Permissions?
* More commands
* More chunk generators
    * [MinestomVanillaGen](https://github.com/Flamgop/MinestomVanillaGen)
    * [TerraGenerationExtension](https://github.com/KrystilizeNevaDies/TerraGenerationExtension)
