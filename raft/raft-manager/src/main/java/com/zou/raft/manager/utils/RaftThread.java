package com.zou.raft.manager.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zys
 * @date 2021/6/21 1:10 下午
 */
@Slf4j
public class RaftThread extends Thread {
    private static final UncaughtExceptionHandler uncaughtExceptionHandler = (t, e)
            -> log.warn("Exception occurred from thread {}", t.getName(), e);

    public RaftThread(String threadName,  Runnable r) {
        super(r, threadName);
        setUncaughtExceptionHandler(uncaughtExceptionHandler);
    }
}
