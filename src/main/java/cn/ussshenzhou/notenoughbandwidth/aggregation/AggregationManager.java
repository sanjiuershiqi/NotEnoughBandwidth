package cn.ussshenzhou.notenoughbandwidth.aggregation;

import cn.ussshenzhou.notenoughbandwidth.util.DefaultChannelPipelineHelper;
import cn.ussshenzhou.notenoughbandwidth.util.PacketUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import io.netty.channel.DefaultChannelPipeline;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author USS_Shenzhou
 */
public class AggregationManager {
    private static final ConcurrentHashMap<Connection, ConcurrentLinkedQueue<AggregatedEncodePacket>> PACKET_BUFFER = new ConcurrentHashMap<>();
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

    public static void takeOver(Packet<?> packet, Connection connection) {
        var type = PacketUtil.getTrueType(packet);
        PACKET_BUFFER.computeIfAbsent(connection, k -> new ConcurrentLinkedQueue<>()).offer(new AggregatedEncodePacket(packet, type));
    }

    private static void flush() {
        PACKET_BUFFER.entrySet().removeIf(e -> !e.getKey().isConnected());
        for (var entry : PACKET_BUFFER.entrySet()) {
            Connection conn = entry.getKey();
            ConcurrentLinkedQueue<AggregatedEncodePacket> queue = entry.getValue();
            if (!queue.isEmpty()) {
                conn.channel().eventLoop().execute(() -> flushInternal(conn, queue));
            }
        }
    }

    public static void flushConnection(Connection connection) {
        ConcurrentLinkedQueue<AggregatedEncodePacket> queue = PACKET_BUFFER.get(connection);
        if (queue != null && !queue.isEmpty()) {
            connection.channel().eventLoop().execute(() -> flushInternal(connection, queue));
        }
    }

    private static void flushInternal(Connection connection, ConcurrentLinkedQueue<AggregatedEncodePacket> queue) {
        try {
            if (queue == null || queue.isEmpty() || !connection.isConnected()) {
                return;
            }
            var encoder = DefaultChannelPipelineHelper.getPacketEncoder((DefaultChannelPipeline) connection.channel().pipeline());
            if (encoder == null) {
                LogUtils.getLogger().error("Failed to get PacketEncoder of connection {} {}.", connection.getDirection(), connection.getRemoteAddress());
                return;
            }
            
            var sendPackets = new ArrayList<AggregatedEncodePacket>();
            AggregatedEncodePacket pkt;
            while ((pkt = queue.poll()) != null) {
                sendPackets.add(pkt);
            }
            
            if (sendPackets.isEmpty()) return;

            connection.send(connection.getSending() == PacketFlow.CLIENTBOUND
                            ? new ClientboundCustomPayloadPacket(new PacketAggregationPacket(sendPackets, encoder.getProtocolInfo(), connection))
                            : new ServerboundCustomPayloadPacket(new PacketAggregationPacket(sendPackets, encoder.getProtocolInfo(), connection)),
                    null, true
            );
        } catch (Exception e) {
            LogUtils.getLogger().error("Skipped: Failed to flush packets.", e);
        }
    }
}
