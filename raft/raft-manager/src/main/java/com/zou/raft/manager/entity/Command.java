package com.zou.raft.manager.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zys
 * @date 2021/6/22 9:47 下午
 */
@Data
public class Command implements Serializable {
    String key;
    String value;
}
