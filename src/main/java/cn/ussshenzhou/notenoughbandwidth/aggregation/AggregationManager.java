package cn.ussshenzhou.notenoughbandwidth.aggregation;

import cn.ussshenzhou.notenoughbandwidth.util.DefaultChannelPipelineHelper;
import cn.ussshenzhou.notenoughbandwidth.util.PacketUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import io.netty.channel.DefaultChannelPipeline;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author USS_Shenzhou
 */
public class AggregationManager {
    private static final WeakHashMap<Connection, ArrayList<Packet<?>>> PACKET_BUFFER = new WeakHashMap<>();
    private static final ScheduledExecutorService TIMER = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("NEB-Flush-thread").setDaemon(true).build());
    private static final ArrayList<ScheduledFuture<?>> TASKS = new ArrayList<>();
    private static volatile boolean initialized = false;

    public synchronized static void init() {
        if (initialized) {
            return;
        }
        initialized = false;
        PACKET_BUFFER.clear();
        TASKS.forEach(task -> task.cancel(false));
        TASKS.clear();
        TASKS.add(TIMER.scheduleAtFixedRate(AggregationManager::flush, 0, AggregationFlushHelper.getFlushPeriodInMilliseconds(), TimeUnit.MILLISECONDS));
        initialized = true;
    }

    public synchronized static void takeOver(Packet<?> packet, Connection connection) {
        PACKET_BUFFER.computeIfAbsent(connection, c -> new ArrayList<>()).add(packet);
    }

    private synchronized static void flush() {
        PACKET_BUFFER.entrySet().removeIf(e -> !e.getKey().isConnected());
        PACKET_BUFFER.forEach(AggregationManager::flushInternal);
    }

    public synchronized static void flushConnection(Connection connection) {
        TIMER.execute(() -> {
            PACKET_BUFFER.entrySet().removeIf(e -> !e.getKey().isConnected());
            flushInternal(connection, PACKET_BUFFER.get(connection));
        });
    }

    private synchronized static void flushInternal(Connection connection, @Nullable ArrayList<Packet<?>> packets) {
        try {
            if (packets == null || packets.isEmpty()) {
                return;
            }
            var encoder = DefaultChannelPipelineHelper.getPacketEncoder((DefaultChannelPipeline) connection.channel().pipeline());
            if (encoder == null) {
                LogUtils.getLogger().error("Failed to get PacketEncoder of connection {} {}.", connection.getDirection(), connection.getRemoteAddress());
                return;
            }
            var sendPackets = new ArrayList<>(packets);
            
            packets.clear();
            connection.flushChannel();
        } catch (Exception e) {
            LogUtils.getLogger().error("Skipped: Failed to flush packets.", e);
        }
    }
}
