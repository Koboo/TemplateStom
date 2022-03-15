package eu.koboo.minestom.server.chunk;

import java.util.List;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.ChunkGenerator;
import net.minestom.server.instance.ChunkPopulator;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FlatGenerator implements ChunkGenerator {

    @Override
    public void generateChunkData(@NotNull final ChunkBatch batch,
            final int chunkX,
            final int chunkZ) {
        for (byte x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
            for (byte z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                batch.setBlock(x, 0, z, Block.BEDROCK);
                for (byte y = 1; y <= 40; y++) {
                    batch.setBlock(x, y, z, Block.STONE);
                }
                batch.setBlock(x, 41, z, Block.DIRT);
                batch.setBlock(x, 42, z, Block.DIRT);
                batch.setBlock(x, 43, z, Block.GRASS_BLOCK);
            }
        }
    }

    @Nullable
    @Override
    public List<ChunkPopulator> getPopulators() {
        return null;
    }

}
