package com.zou.raft.rpc;

import com.zou.raft.rpc.dto.RequestDTO;
import com.zou.raft.rpc.dto.ResponseDTO;

/**
 * @author zys
 * @date 2021/6/19 11:03 下午
 */
public interface RpcServer {
    /**
     * 开始
     */
    void start();

    /**
     * 终结吧！
     */
    void shutdown();

    /**
     * 处理请求
     * @param req
     * @return
     */
    ResponseDTO handlerRequest(RequestDTO req);
}
