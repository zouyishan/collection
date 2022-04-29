package com.zou.raft.manager.exception;

/**
 * @author zys
 * @date 2021/6/22 2:43 下午
 */
public class RaftNotSupportException extends RuntimeException {
    public RaftNotSupportException() {
    }

    public RaftNotSupportException(String message) {
        super(message);
    }
}
