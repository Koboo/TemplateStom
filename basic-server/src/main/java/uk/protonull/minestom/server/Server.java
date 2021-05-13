/*
 * The MIT License (MIT)
 *
 * Copyright (C) 2021 Protonull <protonull@protonmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package uk.protonull.minestom.server;

import uk.protonull.minestom.server.commands.StopCommand;
import java.util.Arrays;
import java.util.List;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.ChunkGenerator;
import net.minestom.server.instance.ChunkPopulator;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Server {

    public static final MinecraftServer SERVER = MinecraftServer.init(); // it's already initialised by Bootstrap

    private static final String DEFAULT_HOST = "0.0.0.0";
    private static final int DEFAULT_PORT = 25565;
    private static final boolean DEFAULT_USE_BASIC = false;

    public static void main(final String[] arguments) {
        try {
            // Setup server
            MinecraftServer.getExceptionManager().setExceptionHandler(Server::exceptionHandler);
            MinecraftServer.getCommandManager().register(new StopCommand());

            // Basic server?
            if (useBasicServer()) {
                MinecraftServer.LOGGER.info("Setting up basic server.");
                final InstanceManager instanceManager = MinecraftServer.getInstanceManager();
                final InstanceContainer instanceContainer = instanceManager.createInstanceContainer();
                instanceContainer.setChunkGenerator(new ChunkGenerator() {
                    @Override
                    public void generateChunkData(@NotNull final ChunkBatch batch,
                                                  final int chunkX,
                                                  final int chunkZ) {
                        for (byte x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
                            for (byte z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                                for (byte y = 0; y < 40; y++) {
                                    batch.setBlock(x, y, z, Block.STONE);
                                }
                                batch.setBlock(x, 41, z, Block.DIRT);
                                batch.setBlock(x, 42, z, Block.DIRT);
                                batch.setBlock(x, 43, z, Block.DIRT);
                                batch.setBlock(x, 44, z, Block.GRASS_BLOCK);
                            }
                        }
                    }
                    @Override
                    public void fillBiomes(@NotNull final Biome[] biomes,
                                           int chunkX,
                                           int chunkZ) {
                        Arrays.fill(biomes, Biome.PLAINS);
                    }
                    @Nullable
                    @Override
                    public List<ChunkPopulator> getPopulators() {
                        return null;
                    }
                });
                final GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
                globalEventHandler.addListener(PlayerLoginEvent.class, (event) -> {
                    final Player player = event.getPlayer();
                    event.setSpawningInstance(instanceContainer);
                    player.setRespawnPoint(new Pos(0, 45, 0));
                });
            }

            // Start the server
            calledBeforeStart();
            final String host = getHost();
            final int port = getPort();
            SERVER.start(host, port);
            MinecraftServer.LOGGER.info("Server: " + host + ":" + port);
        } catch (final Throwable thrown) {
            MinecraftServer.LOGGER.error("An error occurred while trying to start the server.", thrown);
            System.exit(1); // Do not fallback to Bootstrap
        }
    }

    private static void exceptionHandler(final Throwable thrown) {
        MinecraftServer.LOGGER.warn("", thrown);
    }

    private static String getHost() {
        return System.getProperty("host", DEFAULT_HOST);
    }

    private static int getPort() {
        try {
            return Integer.parseInt(System.getProperty("port", Integer.toString(DEFAULT_PORT)));
        }
        catch (final NullPointerException | NumberFormatException ignored) {
            return DEFAULT_PORT;
        }
    }

    private static boolean useBasicServer() {
        try {
            return Boolean.parseBoolean(System.getProperty("useBasic", Boolean.toString(DEFAULT_USE_BASIC)));
        }
        catch (final NullPointerException | NumberFormatException ignored) {
            return DEFAULT_USE_BASIC;
        }
    }

    /**
     * This is purely a mixin hook. Use this to inject code prior to the server being initiated.
     */
    public static void calledBeforeStart() {

    }

}
