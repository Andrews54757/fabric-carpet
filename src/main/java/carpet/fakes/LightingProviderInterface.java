package carpet.fakes;

import net.minecraft.world.chunk.light.ChunkLightProvider;

public interface LightingProviderInterface
{
    void setBlockLightProvider(ChunkLightProvider provider);

    void setSkyLightProvider(ChunkLightProvider provider);
}
