# Minestom Server Template

A very simple implementation of Minestom, which can be used as a template to develop your own
server. The project structure allows to create a separate API at the same time, which can be
released apart from the actual server.

## Specification

* JDK: ``Java 17``
* Minecraft-Version: ``1.18.2``
* Gradle: ``7.2``

## Credits

A big thanks to [Project Cepi](https://github.com/Project-Cepi), for
open-sourcing [Sabre](https://github.com/Project-Cepi/Sabre)!

## Features

* ``stop``-Command to stop the server (Permission: ``command.stop``)
* Asynchronous writing of Log files
* ColorCode support in the Console
* Proxy support with Configuration (BungeeCord, Waterfall, Velocity)
* Project structure to support API building

## File Linking

* [API Dependencies](server-api/build.gradle)
* [App Dependencies](server-app/build.gradle)
* [Launch](server-app/src/main/java/eu/koboo/minestom/Launch.java)

## Contributions

If you want to contribute, pull requests are always welcome!
