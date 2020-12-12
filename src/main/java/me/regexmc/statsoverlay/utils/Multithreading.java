/**
 * @author Sk1er LCC
 */

package me.regexmc.statsoverlay.utils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Multithreading {
    public static final ThreadPoolExecutor POOL;
    private static final AtomicInteger counter = new AtomicInteger(0);
    private static final ScheduledExecutorService RUNNABLE_POOL = Executors.newScheduledThreadPool(10, (r) -> new Thread(r, "ModCore Thread " + counter.incrementAndGet()));

    static {
        POOL = new ThreadPoolExecutor(50, 50, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue(), (r) -> new Thread(r, String.format("Thread %s", counter.incrementAndGet())));
    }

    public Multithreading() {
    }

    public static ScheduledFuture<?> schedule(Runnable r, long initialDelay, long delay, TimeUnit unit) {
        return RUNNABLE_POOL.scheduleAtFixedRate(r, initialDelay, delay, unit);
    }

    public static ScheduledFuture<?> schedule(Runnable r, long delay, TimeUnit unit) {
        return RUNNABLE_POOL.schedule(r, delay, unit);
    }

    public static void runAsync(Runnable runnable) {
        POOL.execute(runnable);
    }

    public static Future<?> submit(Runnable runnable) {
        return POOL.submit(runnable);
    }
}
