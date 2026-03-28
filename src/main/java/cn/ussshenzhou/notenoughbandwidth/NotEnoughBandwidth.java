package cn.ussshenzhou.notenoughbandwidth;

import cn.ussshenzhou.notenoughbandwidth.config.ConfigHelper;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.eventbus.api.IEventBus;
import org.slf4j.Logger;
import cn.ussshenzhou.notenoughbandwidth.aggregation.AggregationManager;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * @author USS_Shenzhou
 */
@Mod(ModConstants.MOD_ID)
public class NotEnoughBandwidth {
    private static final Logger LOGGER = LogUtils.getLogger();

    public NotEnoughBandwidth() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        ConfigHelper.loadConfig(new NotEnoughBandwidthConfig());
    }

    private void setup(final FMLCommonSetupEvent event) {
        AggregationManager.init();
    }


}
