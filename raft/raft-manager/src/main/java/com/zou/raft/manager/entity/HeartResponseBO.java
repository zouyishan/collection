package com.zou.raft.manager.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zys
 * @date 2021/6/21 2:46 下午
 */
@Data
public class HeartResponseBO implements Serializable {
    /**
     * 当前的任期号，用于领导人去更新自己
     */
    private Long term;

    /**
     * 更随者包含了匹配上 prevLogIndex 和 prevLogTerm 的日志时为真
     */
    private Boolean success;
}
