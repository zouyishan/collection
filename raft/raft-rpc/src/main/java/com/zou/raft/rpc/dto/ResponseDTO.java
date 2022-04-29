package com.zou.raft.rpc.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zys
 * @date 2021/6/19 10:53 下午
 */
@Data
public class ResponseDTO<T> implements Serializable {
    private T result;

    public ResponseDTO(T result) {
        this.result = result;
    }

    public static ResponseDTO ok() {
        return new ResponseDTO<>("OK");
    }

    public static ResponseDTO fail() {
        return new ResponseDTO<>("Fail");
    }
}
