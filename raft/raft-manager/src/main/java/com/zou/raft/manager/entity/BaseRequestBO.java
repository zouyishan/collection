package com.zou.raft.manager.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zys
 * @date 2021/6/19 10:49 下午
 */
@Data
public class BaseRequestBO implements Serializable {
    /**
     * 候选人任期号
     */
    private Long term;

    /**
     * 被请求人ID(ip:selfPort)
     */
    private String serverId;
}
