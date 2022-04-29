package com.zou.raft.manager.utils;


import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * @author zys
 * @date 2021/6/21 1:04 下午
 */
@Slf4j
public class RaftThreadPool {
    public static final int CPU = Runtime.getRuntime().availableProcessors();
    public static final int MAX_POOL_SIZE = CPU * 2;
    public static final int QUEUE_SIZE = 1024;
    public static final long KEEP_TIME = 1000 * 60;
    public static TimeUnit KEEP_TIME_UNIT = TimeUnit.MILLISECONDS;

    private static ThreadPoolExecutor THREAD_POOL = getThreadPool();
    private static ScheduledExecutorService SCHEDULE_EXECUTOR = getScheduleExecutor();

    // 获取定期线程
    private static ScheduledExecutorService getScheduleExecutor() {
        return new ScheduledThreadPoolExecutor(CPU, new NameThreadFactory());
    }

    public static void scheduleAtFixedRate(Runnable runnable, long initDelay, long delay) {
        SCHEDULE_EXECUTOR.scheduleAtFixedRate(runnable, initDelay, delay, TimeUnit.MILLISECONDS);
    }

    public static void scheduleWithFixedDelay(Runnable runnable, long delay) {
        SCHEDULE_EXECUTOR.scheduleWithFixedDelay(runnable, 0, delay, TimeUnit.MILLISECONDS);
    }

    // 获取线程池
    private static ThreadPoolExecutor getThreadPool() {
        return new RaftThreadPoolExecutor(CPU, MAX_POOL_SIZE, KEEP_TIME, KEEP_TIME_UNIT,
                new LinkedBlockingDeque<>(QUEUE_SIZE), new NameThreadFactory());
    }

    /**
     * 线程池中执行
     * @param runnable
     */
    public static void execute(Runnable runnable) {
        THREAD_POOL.execute(runnable);
    }

    /**
     * fromPool表示是否不在线程池中执行
     * @param runnable
     * @param fromPool
     */
    public static void execute(Runnable runnable, boolean fromPool) {
        if (fromPool) {
            runnable.run();
        } else {
            THREAD_POOL.execute(runnable);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Future<T> submit(Callable callable) {
        return THREAD_POOL.submit(callable);
    }

    static class NameThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new RaftThread("Raft thread", r);
            t.setDaemon(true);
            t.setPriority(5);
            return t;
        }
    }
}
