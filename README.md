# Minestom Server Template

A very simple implementation of [Minestom](https://minestom.net), which can be used as a template to
develop your own server. The project structure allows you to create a separate API, which can be
released apart from the actual server.

## Specification

This template can also be run as a server. You can
either [build from source](https://github.com/Koboo/TemplateStom/archive/refs/heads/main.zip) or
download the [latest release](https://github.com/Koboo/TemplateStom/releases/latest).

* JDK: ``Java 17``
* Minecraft-Version: ``1.18.2``
* Gradle: ``7.2``

## Features

* ``stop``-Command to stop the server (Permission: ``command.stop``)
* ``version``-Command to show current server version
* Asynchronous writing of log-files
* [ColorCode](https://minecraft.fandom.com/wiki/Formatting_codes) support in the Console
* Proxy support (BungeeCord, Waterfall, Velocity)
* Structure to publish separate API
* YAML configuration file (``server_config.yml``)

## Publishing

If the API should be published via ``maven-publish``, then you have to configure it
in it's [build.gradle](server-api/build.gradle).

## File Linking

Links to the most interesting files

* [API Dependencies](server-api/build.gradle)
* [App Dependencies](server-app/build.gradle)
* [Launch](server-app/src/main/java/eu/koboo/minestom/Launch.java)
* [Dependency Versions](gradle.properties)

## Credits

A big thanks to these dudes!

* [Project Cepi](https://github.com/Project-Cepi), author
  of [Sabre](https://github.com/Project-Cepi/Sabre)
* [KlainStom](https://github.com/KlainStom), author
  of [microstom](https://github.com/KlainStom/microstom)
* [ZakShearman](https://github.com/ZakShearman), author
  of [Operadora](https://github.com/ZakShearman/Operadora)
* [Minestom wiki](https://wiki.minestom.net/)
* [Minestom javadocs](https://javadoc.minestom.net/)

## Contributions

If you want to contribute, pull requests are always welcome!

## Server Configuration

This is the default configuration when the server gets started for the first time:

````yaml
server:
  # The host address of the server
  host: 0.0.0.0
  # The listening port of the server
  port: 25568
  # Toggle online-mode (mojang authentication) of the server
  online-mode: true
  # Toggle difficulty (options: PEACEFUL, EASY, NORMAL, HARD)
  difficulty: NORMAL
proxy:
  # Toggle proxy-mode (options: NONE, BUNGEECORD, VELOCITY)
  proxy-mode: NONE
  # Set your velocity-secret (Do not share that!)
  velocity-secret: ''
packets:
  # Set the rate-limit of packets/second for the clients (0 disables rate-limit)
  rate-limit: 450
  # Set the max-size of packets from the clients (maximum is 2097151 bytes)
  max-size: 2097151
  # Set the compression-threshold of packets (0 disables compression)
  compression-threshold: ''
view-distance:
  # Set the view-distance of chunks (range between 2 and 32)
  chunks: 10
  # Set the view-distance of entities (range between 2 and 32)
  entities: 10
````

**Note: The configuration cannot be reloaded after the start. A restart must be performed for
changes to be applied.**