package cn.ussshenzhou.notenoughbandwidth.mixin;

import cn.ussshenzhou.notenoughbandwidth.chunk.CachedChunkTrackingView;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * @author USS_Shenzhou
 */
@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {
    @Shadow
    @Final
    private net.minecraft.world.level.ChunkPos.Tracker chunkTracker;
    @Shadow
    @Final
    private ServerLevel level;

    /**
     * @author Burning_TNT
     * @reason NEB overwrites original chunk map update strategy.
     */
    @Overwrite
    public void updatePlayerStatus(ServerPlayer player, boolean track) {
        if (player.level != this.level) {
            return;
        }

        CachedChunkTrackingView.onUpdateChunkTracking(player, getPlayerViewDistance(player), new CachedChunkTrackingView.Context() {
            @Override
            public void startChunkTracking(ChunkPos pos) {
                updateChunkTracking(player, pos, new net.minecraft.network.protocol.Packet<?>[]{new net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket(level.getChunk(pos.x, pos.z), level.getLightEngine(), null, null, true)}, true, true);
            }

            @Override
            public void stopChunkTracking(ChunkPos pos) {
                updateChunkTracking(player, pos, new net.minecraft.network.protocol.Packet<?>[0], false, true);
            }

            @Override
            public void putTicket(ChunkPos pos, int ticks) {
                // Not supported in 1.19.2 without DistanceManager
            }
        });
    }

    @Shadow
    protected abstract int getPlayerViewDistance(ServerPlayer player);

    @Shadow
    protected abstract void updateChunkTracking(ServerPlayer player, ChunkPos pos, net.minecraft.network.protocol.Packet<?>[] packets, boolean track, boolean isUnloading);
}
