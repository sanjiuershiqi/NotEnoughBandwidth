package cn.ussshenzhou.notenoughbandwidth.mixin;

import cn.ussshenzhou.notenoughbandwidth.stat.SimpleStatManager;
import cn.ussshenzhou.notenoughbandwidth.util.PacketUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * @author USS_Shenzhou
 */
@Mixin(PacketDecoder.class)
public class PacketDecoderMixin {

    @Inject(method = "decode",
            at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", shift = At.Shift.BEFORE))
    private void nebRecordIn(ChannelHandlerContext ctx, ByteBuf input, List<Object> out, CallbackInfo ci) {
        // Approximate packet size by looking at readerIndex progress
        // This is a simplified version for 1.19.2 port
        int size = input.readerIndex();
        SimpleStatManager.inBaked(size);
        SimpleStatManager.inRaw(size);
    }
}
