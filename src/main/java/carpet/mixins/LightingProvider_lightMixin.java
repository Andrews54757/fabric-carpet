package carpet.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import carpet.fakes.LightingProviderInterface;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import net.minecraft.world.chunk.light.LightingProvider;

@Mixin(LightingProvider.class)
public abstract class LightingProvider_lightMixin extends LightingProvider implements LightingProviderInterface
{
    public LightingProvider_lightMixin(ChunkProvider chunkProvider, boolean hasBlockLight, boolean hasSkyLight)
    {
        super(chunkProvider, hasBlockLight, hasSkyLight);
    }

    @Shadow @Final
    ChunkLightProvider<?, ?> blockLightProvider;

    @Shadow @Final
    ChunkLightProvider<?, ?> skyLightProvider;

    public void setBlockLightProvider(ChunkLightProvider provider) {
        blockLightProvider = provider;
    }
    
    public void setSkyLightProvider(ChunkLightProvider provider) {
        skyLightProvider = provider;
    }

}
