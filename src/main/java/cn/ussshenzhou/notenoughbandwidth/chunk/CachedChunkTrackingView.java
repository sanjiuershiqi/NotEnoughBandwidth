package cn.ussshenzhou.notenoughbandwidth.chunk;

import cn.ussshenzhou.notenoughbandwidth.NotEnoughBandwidthConfig;
import cn.ussshenzhou.notenoughbandwidth.config.ConfigHelper;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongMaps;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * A {@link ChunkTrackingView} implementation that adds a lightweight,
 * time-based cache on top of a primary ("major") tracking view.
 *
 * <p>This cache is used to temporarily retain chunks that have recently
 * left the major view, in order to reduce frequent enter/leave churn when
 * the player moves near view boundaries.</p>
 *
 * <p>A cached chunk will be evicted if <strong>any</strong> of the following
 * conditions is met:</p>
 * <ul>
 *   <li>It has not been within the major view boundary for a configured
 *       amount of time (timeout).</li>
 *   <li>It is farther than the allowed cache distance from the current
 *       view center.</li>
 *   <li>The cache exceeds the configured size limit, in which case the
 *       oldest cached chunks are removed first.</li>
 * </ul>
 */
public class CachedChunkTrackingView {
    private static final long NO_CACHE = -1L;

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * The primary {@link ChunkTrackingView.Positioned} that represents
     * the authoritative chunk visibility range.
     *
     * <p>All chunks contained in this view are considered visible regardless
     * of the cache state.</p>
     */
    private ChunkTrackingView.Positioned major;

    /**
     * A cache mapping packed {@link ChunkPos} values to the timestamp (in millis)
     * when they were last visible.
     *
     * <p>The map preserves insertion order, allowing efficient eviction of
     * the oldest cached entries when size or timeout constraints are exceeded.</p>
     *
     * <p>The default return value is {@link #NO_CACHE}, allowing fast
     * existence checks without boxing.</p>
     *
     * <p><b>Note:</b> This could potentially be replaced by a value-record
     * representation once Project Valhalla becomes available.</p>
     */
    private final Long2LongLinkedOpenHashMap cache = new Long2LongLinkedOpenHashMap();

    // Stubs for remaining compilation errors
    public CachedChunkTrackingView() {
    }

    public boolean contains(int x, int z, boolean includeNeighbors) {
        return cache.containsKey(ChunkPos.asLong(x, z));
    }

    public void forEach(@NotNull Consumer<ChunkPos> consumer) {
        LongIterator cacheIterator = this.cache.keySet().iterator();
        while (cacheIterator.hasNext()) {
            consumer.accept(new ChunkPos(cacheIterator.nextLong()));
        }
    }

    public interface Context {
        void startChunkTracking(ChunkPos pos);

        void stopChunkTracking(ChunkPos pos);

        void putTicket(ChunkPos pos, int ticks);
    }

    /**
     * Updates the chunk tracking view for a player, applying cached tracking
     * semantics when possible.
     *
     * <p>If the player's view center or view distance changes, a new
     * {@link ChunkTrackingView.Positioned} is created and synchronized to
     * the client.</p>
     *
     * <p>If the current tracking view already supports caching, it will be
     * updated in-place; otherwise, a new {@link CachedChunkTrackingView} is
     * created and installed.</p>
     *
     * @param player             the player whose chunk tracking is being updated
     * @param playerViewDistance the player's view distance
     */
    public static void onUpdateChunkTracking(ServerPlayer player, int playerViewDistance, Context context) {
        // Simplified DCC implementation for 1.19.2
        // Since ChunkTrackingView doesn't exist, we fall back to a simpler approach or just use the context directly
        ChunkPos playerChunkPosition = player.chunkPosition();
        
        // Always set cache center in 1.19.2
        player.connection.send(new ClientboundSetChunkCacheCenterPacket(playerChunkPosition.x, playerChunkPosition.z));
        
        // This is a stub for the 1.19.2 port.
        // Full DCC requires tracking all loaded chunks per player manually in 1.19.2
        // For now, we rely on the vanilla updateChunkTracking which was called before this
    }

    // Stub out tick logic as it depends on 1.21 specific features
    // We will revisit this when fully implementing DCC for 1.19.2
    public void tick() {
    }

    @FunctionalInterface
    private interface CacheConsumer {
        byte CONTINUE = 0, REMOVE = 1, STOP = 2;

        @MagicConstant(flags = {CONTINUE, REMOVE, STOP})
        byte accept(long pos, long time);
    }

    private void enumerate(CacheConsumer consumer) {
        ObjectIterator<Long2LongMap.Entry> iterator = cache.long2LongEntrySet().fastIterator();
        while (iterator.hasNext()) {
            Long2LongMap.Entry entry = iterator.next();

            byte v = consumer.accept(entry.getLongKey(), entry.getLongValue());
            if ((v & CacheConsumer.REMOVE) != 0) {
                iterator.remove();
            }
            if ((v & CacheConsumer.STOP) != 0) {
                return;
            }
        }
    }
}