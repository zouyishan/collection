package com.zou.raft.manager.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zys
 * @date 2021/6/28 5:29 下午
 */
@Data
// 天坑，记住加序列化
public class ClientKVReq implements Serializable {
    String key;
    String value;
}
