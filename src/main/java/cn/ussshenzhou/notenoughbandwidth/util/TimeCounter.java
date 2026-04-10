package cn.ussshenzhou.notenoughbandwidth.util;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.util.Util;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author USS_Shenzhou
 */
public class TimeCounter {
    private final Long2IntOpenHashMap container = new Long2IntOpenHashMap();
    private final int windowsSizeMs;

    private long lastUpdateTime = 0;

    public TimeCounter(int windowsSizeMs) {
        this.windowsSizeMs = windowsSizeMs;
    }

    public TimeCounter() {
        this(2000);
    }

    private synchronized void update() {
        long now = Util.getMillis();
        // Limit cleanup frequency to at most once every 100ms
        if (now - lastUpdateTime < 100) {
            return;
        }
        lastUpdateTime = now;
        container.keySet().removeIf(then -> now - then > windowsSizeMs);
    }

    public synchronized void put(int value) {
        // Just add value, use the current time, and handle collision by accumulating
        long now = Util.getMillis();
        int existing = container.getOrDefault(now, 0);
        container.put(now, existing + value);
        update();
    }

    public synchronized double averageIn1s() {
        return container.values().intStream().sum() / (double) windowsSizeMs * 1000;
    }
}
