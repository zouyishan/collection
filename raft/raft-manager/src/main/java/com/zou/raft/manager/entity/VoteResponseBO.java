package com.zou.raft.manager.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zys
 * @date 2021/6/19 10:50 下午
 */
@Data
public class VoteResponseBO implements Serializable {
    /**
     * 当前任期号,以便候选人跟新用
     */
    private Long term;

    /**
     * 候选人赢得了这张票就为真
     */
    private Boolean isVoted;
}
