package cn.ussshenzhou.notenoughbandwidth.util;

import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class PacketUtil {
    public static ResourceLocation getTrueType(Packet<?> packet) {
        return new ResourceLocation("minecraft", packet.getClass().getSimpleName().toLowerCase());
    }

    public static Object getTruePacket(Packet<?> packet) {
        return packet;
    }
}
