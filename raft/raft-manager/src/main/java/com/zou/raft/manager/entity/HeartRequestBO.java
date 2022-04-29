package com.zou.raft.manager.entity;

import lombok.Data;

/**
 * @author zys
 * @date 2021/6/21 12:57 下午
 */
@Data
public class HeartRequestBO extends BaseRequestBO {
    /**
     * 领导人的Id，以便于跟随重定向请求
     */
    private String leaderId;

    /**
     * prevLogIndex 前面条目的index
     */
    private Long prevLogIndex;

    /**
     * prevLogTerm 前面条目的任期号
     */
    private Long preLogTerm;

    /**
     * Leader已经提交的日志索引
     */
    private Long leaderCommit;

    /**
     * 准备的日志条目，表示心跳时为空，一次性发送多个是为了提高效率
     */
    LogEntryBO[] entries;
}
