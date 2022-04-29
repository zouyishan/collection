package com.zou.raft.biz;

/**
 * @author zys
 * @date 2021/6/21 4:31 下午
 */
public interface LifeCycle {
    void init() throws Throwable;

    void destroy() throws Throwable;
}
