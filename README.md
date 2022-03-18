# Minestom Server Template

A very simple implementation of Minestom, which can be used as a template to develop your own
server. The project structure allows to create a separate API at the same time, which can be
released apart from the actual server.

## Specification

* JDK: ``Java 17``
* Minecraft-Version: ``1.18.2``
* Gradle: ``7.2``

## Features

* ``stop``-Command to stop the server (Permission: ``command.stop``)
* Asynchronous writing of log-files
* [ColorCode](https://minecraft.fandom.com/wiki/Formatting_codes) support in the Console
* Proxy support (BungeeCord, Waterfall, Velocity)
* Structure to publish separate API
* YAML configuration file (``server_config.yml``)

## Publishing

If the API should be published via ``maven-publish``, it's [build.gradle](server-api/build.gradle) must be modified accordingly.

## File Linking

Links to the most searched files

* [API Dependencies](server-api/build.gradle)
* [App Dependencies](server-app/build.gradle)
* [Launch](server-app/src/main/java/eu/koboo/minestom/Launch.java)
* [Dependency Versions](gradle.properties)

## Credits

A big thanks to [Project Cepi](https://github.com/Project-Cepi), for
open-sourcing [Sabre](https://github.com/Project-Cepi/Sabre)!

## Contributions

If you want to contribute, pull requests are always welcome!

## Server Configuration

This is the default configuration when the server gets started for the first time:

````yaml
server:
  # The host address of the server
  host: 0.0.0.0
  # The listening port of the server
  port: 25565
  # Toggle online-mode (mojang authentication) of the server
  online-mode: true
proxy:
  # Toggle proxy-mode of the server (options: NONE, BUNGEECORD, VELOCITY)
  proxy-mode: NONE
  # Set your velocity-secret
  velocity-secret: ''
````