package com.zou.raft.manager.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zys
 * @date 2021/6/21 2:34 下午
 */
@Slf4j
public class SleepHelper {
    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            log.warn("sleep execption {}", e.getMessage());
        }
    }
}
