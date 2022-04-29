package com.zou.raft.manager.entity;

import lombok.Data;

/**
 * 一个节点的信息
 * @author zys
 * @date 2021/6/20 1:05 下午
 */
@Data
public class Peer {
    /**
     * ip:selfPort 表示一个对应的列表吧
     */
    private String addr;

    public Peer(String addr) {
        this.addr = addr;
    }
}
