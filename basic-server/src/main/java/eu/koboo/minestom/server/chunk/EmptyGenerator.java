package eu.koboo.minestom.server.chunk;

import java.util.List;
import net.minestom.server.instance.ChunkGenerator;
import net.minestom.server.instance.ChunkPopulator;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EmptyGenerator implements ChunkGenerator {

    @Override
    public void generateChunkData(
            @NotNull ChunkBatch batch,
            int chunkX, int chunkZ) {
        batch.setBlock(8, 0, 8, Block.AIR);
    }

    @Override
    public @Nullable List<ChunkPopulator> getPopulators() {
        return null;
    }
}
