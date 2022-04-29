package com.zou.raft.manager.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zys
 * @date 2021/6/28 5:28 下午
 */
@Data
// 天坑，记住加序列化
public class ClientKVAck implements Serializable {
    Object result;

    public ClientKVAck(Object result) {
        this.result = result;
    }
}
