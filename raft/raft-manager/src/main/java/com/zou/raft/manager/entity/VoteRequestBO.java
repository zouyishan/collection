package com.zou.raft.manager.entity;

import lombok.Data;

/**
 * @author zys
 * @date 2021/6/19 10:50 下午
 */
@Data
public class VoteRequestBO extends BaseRequestBO {
    /**
     * 请求选票的候选人ID(ip:serverPort)
     */
    private String candidateId;
}
