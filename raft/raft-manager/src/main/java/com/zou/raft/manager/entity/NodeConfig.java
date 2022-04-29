package com.zou.raft.manager.entity;

import lombok.Data;

import java.util.List;

/**
 * @author zys
 * @date 2021/6/20 3:13 下午
 */
@Data
public class NodeConfig {
    /**
     * 自身的port
     */
    private Integer selfPort;

    /**
     * 所有的地址列表
     */
    private List<String> peerAddrs;
}
