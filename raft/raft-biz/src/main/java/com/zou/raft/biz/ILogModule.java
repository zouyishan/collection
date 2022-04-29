package com.zou.raft.biz;

import com.zou.raft.manager.entity.LogEntryBO;

/**
 * 操作日志模块
 *
 * @author zys
 * @date 2021/6/22 9:43 下午
 */
public interface ILogModule {
    /**
     * 写一个日志
     * @param logEntryBO
     * @return
     */
    void write(LogEntryBO logEntryBO);

    /**
     * 通过index查询日志
     * @param index
     * @return
     */
    LogEntryBO read(Long index);

    /**
     * 从某处开始删除前面的,用作失败回滚
     * @param startIndex
     * @return
     */
    void removeOnStartIndex(Long startIndex);

    /**
     * 获取日志最后一个LogEntryBO
     * @return
     */
    LogEntryBO getLast();

    /**
     * 获取LogEntryBO的最后的index值
     * @return
     */
    Long getLastIndex();
}
