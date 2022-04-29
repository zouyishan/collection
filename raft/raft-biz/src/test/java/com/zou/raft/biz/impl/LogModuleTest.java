package com.zou.raft.biz.impl;

import com.zou.raft.manager.entity.LogEntryBO;

/**
 * @author zys
 * @date 2021/6/25 5:06 下午
 */
public class LogModuleTest {
    public static void main(String[] args) {
        LogModuleImpl instance = LogModuleImpl.getInstance();
        LogEntryBO logEntryBO = new LogEntryBO();
        logEntryBO.setIndex(2L);
        logEntryBO.setTerm(1L);
        instance.write(logEntryBO);
        System.out.println(instance.getLastIndex());
    }
}
