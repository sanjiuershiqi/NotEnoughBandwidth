package cn.ussshenzhou.notenoughbandwidth.util;

import net.minecraft.util.Util;

import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A highly optimized, lock-free ring-buffer implementation of a time-based counter.
 * Resolves severe server TPS lag during chunk loads by eliminating HashMap and O(N) cleanup.
 *
 * @author USS_Shenzhou
 */
public class TimeCounter {
    // 2000 milliseconds window size default
    private final int windowsSizeMs;
    // We break the window into discrete 'slots' representing milliseconds (e.g. 2000 slots)
    private final AtomicIntegerArray slots;
    private final AtomicLong lastUpdatedSlot = new AtomicLong(0);

    public TimeCounter(int windowsSizeMs) {
        this.windowsSizeMs = windowsSizeMs;
        // Ring buffer holding the byte sum for each millisecond slot
        this.slots = new AtomicIntegerArray(windowsSizeMs);
    }

    public TimeCounter() {
        this(2000);
    }

    /**
     * Clear old data if time has advanced since the last update.
     */
    private void advanceTime(long now) {
        long last = lastUpdatedSlot.get();
        if (now <= last) {
            return;
        }

        // Try to atomically update the last updated time
        if (lastUpdatedSlot.compareAndSet(last, now)) {
            // How many milliseconds have passed since we last updated?
            long delta = now - last;
            
            // If the time jump is larger than the window, clear the whole array
            if (delta >= windowsSizeMs) {
                for (int i = 0; i < windowsSizeMs; i++) {
                    slots.set(i, 0);
                }
            } else {
                // Otherwise, clear only the slots we just passed over
                for (long t = last + 1; t <= now; t++) {
                    int slotIndex = (int) (t % windowsSizeMs);
                    slots.set(slotIndex, 0);
                }
            }
        } else {
            // Another thread is currently advancing time, just let it do the work
            // Since this is a fast path, we don't need to spin-wait here
        }
    }

    public void put(int value) {
        if (value <= 0) return;
        long now = Util.getMillis();
        advanceTime(now);
        
        int slotIndex = (int) (now % windowsSizeMs);
        // Atomically add the bytes to the current millisecond slot
        slots.addAndGet(slotIndex, value);
    }

    public double averageIn1s() {
        long now = Util.getMillis();
        advanceTime(now);

        // Calculate sum over the whole window (O(N) but array read is extremely fast and array is small: 2000 elements)
        long totalSum = 0;
        for (int i = 0; i < windowsSizeMs; i++) {
            totalSum += slots.get(i);
        }
        
        return totalSum / (double) windowsSizeMs * 1000;
    }
}
