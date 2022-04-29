package com.zou.raft.manager.constant;

/**
 * @author zys
 * @date 2021/6/19 10:48 下午
 */
public class RaftConstants {
    /**
     * 请求投票
     */
    public static final int REQUEST_VOTE = 0;

    /**
     * 心跳包
     */
    public static final int HEART_BEAT = 1;

    /**
     * 客户端发送给leader的请求
     */
    public static final int CLIENT_REQ = 2;

    /**
     * 默认20000毫秒超时
     */
    public static final int TIMEOUT = 20000;

    /**
     * 获取最后的键
     */
    public final static byte[] LAST_INDEX_KEY = "LAST_INDEX_KEY".getBytes();

    /**
     * 给client的put请求
     */
    public static final int PUT = 0;

    /**
     * 给client的get请求
     */
    public static final int GET = 1;
}
