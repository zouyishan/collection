package com.zou.raft.biz;

import com.zou.raft.manager.entity.LogEntryBO;

/**
 * 状态机
 *
 * @author zys
 * @date 2021/6/22 9:44 下午
 */
public interface IStateMachine {
    /**
     * 确认状态机
     * @param logEntryBO
     */
    void apply(LogEntryBO logEntryBO);

    /**
     * 通过key获取LogEntryBO
     * @param key
     * @return
     */
    LogEntryBO get(String key);

    /**
     * 通过key获取value
     * @param key
     * @return
     */
    String getString(String key);

    /**
     * 设置key -》 value
     * @param key
     * @param value
     */
    void setString(String key, String value);

    /**
     * 批量删除key
     * @param key
     */
    void delString(String... key);

}
