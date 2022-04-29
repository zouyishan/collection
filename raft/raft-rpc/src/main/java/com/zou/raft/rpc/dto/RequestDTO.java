package com.zou.raft.rpc.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 可能会有不同的请求类型，这里用T表示
 *
 * @author zys
 * @date 2021/6/19 10:53 下午
 */
@Data
public class RequestDTO<T> implements Serializable {
    /**
     * 请求类型
     */
    private Integer cmd = -1;

    /**
     * 请求路径
     */
    private String url;

    /**
     * 请求体
     */
    private T obj;
}
