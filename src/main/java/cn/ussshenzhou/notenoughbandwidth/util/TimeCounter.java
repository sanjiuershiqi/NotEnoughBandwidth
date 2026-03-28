package cn.ussshenzhou.notenoughbandwidth.util;

import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author USS_Shenzhou
 */
public class TimeCounter {
    private final ConcurrentSkipListMap<Long, Integer> container = new ConcurrentSkipListMap<>();

    public TimeCounter() {
    }

    public double averageIn1s() {
        long now = System.currentTimeMillis();
        container.headMap(now - 1000).clear();
        return container.values().stream().mapToInt(Integer::intValue).average().orElse(0);
    }

    public void put(int value) {
        container.put(System.currentTimeMillis(), value);
    }
}
