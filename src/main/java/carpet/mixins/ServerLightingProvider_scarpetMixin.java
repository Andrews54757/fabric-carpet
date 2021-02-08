package carpet.mixins;

import carpet.fakes.ServerLightingProviderInterface;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.light.LightingProvider;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerLightingProvider.class)
public abstract class ServerLightingProvider_scarpetMixin extends LightingProvider implements ServerLightingProviderInterface
{
    @Shadow @Final volatile int taskBatchSize;

    @Shadow public abstract void checkBlock(BlockPos pos);

    private boolean blockLight;
    private boolean skyLight;

    public ServerLightingProvider_scarpetMixin(ChunkProvider chunkProvider, boolean hasBlockLight, boolean hasSkyLight)
    {
        super(chunkProvider, hasBlockLight, hasSkyLight);

        this.blockLight = hasBlockLight;
        this.skyLight = hasSkyLight;
    }

    @Override
    public boolean hasBlockLight() {
        return blockLight;
    }

    @Override
    public boolean hasSkyLight() {
        return skyLight;
    }

    @Override
    public void setBlockLight(ChunkProvider provider, boolean value) {
        
    }

    @Override
    public void setSkyLight(ChunkProvider provider, boolean value) {
        
    }

    @Override
    public int getTaskBatchSize()
    {
        return taskBatchSize;
    }
    
    @Override
    public void resetLight(Chunk chunk, ChunkPos pos)
    {
        //super.setRetainData(pos, false);
        //super.setLightEnabled(pos, false);
        //for (int x = chpos.x-1; x <= chpos.x+1; x++ )
        //    for (int z = chpos.z-1; z <= chpos.z+1; z++ )
            {
                //ChunkPos pos = new ChunkPos(x, z);
                int j;
                for(j = -1; j < 17; ++j) {                                                                 // skip some recomp
                    super.enqueueSectionData(LightType.BLOCK, ChunkSectionPos.from(pos, j), new ChunkNibbleArray(), false);
                    super.enqueueSectionData(LightType.SKY, ChunkSectionPos.from(pos, j), new ChunkNibbleArray(), false);
                }
                for(j = 0; j < 16; ++j) {
                    super.setSectionStatus(ChunkSectionPos.from(pos, j), true);
                }

                super.setColumnEnabled(pos, true);

                    chunk.getLightSourcesStream().forEach((blockPos) -> {
                        super.addLightSource(blockPos, chunk.getLuminance(blockPos));
                    });

            }




    }
}
