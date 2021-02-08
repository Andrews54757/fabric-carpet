package carpet.fakes;

import java.util.concurrent.CompletableFuture;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkProvider;

public interface ServerLightingProviderInterface
{
    int getTaskBatchSize();

    void invokeUpdateChunkStatus(ChunkPos pos);

    void removeLightData(Chunk chunk);

    CompletableFuture<Void> relight(Chunk chunk);

    void resetLight(Chunk chunk, ChunkPos pos);

    boolean hasSkyLight();

    boolean hasBlockLight();

    void setSkyLight(ChunkProvider provider, boolean value);

    void setBlockLight(ChunkProvider provider, boolean value);
}
