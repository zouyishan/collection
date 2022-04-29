package com.zou.raft.rpc;

import com.zou.raft.rpc.dto.RequestDTO;
import com.zou.raft.rpc.dto.ResponseDTO;

/**
 * @author zys
 * @date 2021/6/19 11:03 下午
 */
public interface RpcClient {
    /**
     * 发送请求，有默认超时为200000
     * @param request
     * @return
     */
    ResponseDTO send(RequestDTO request);

    /**
     * 发送请求，带超时
     * @param request
     * @param timeout
     * @return
     */
    ResponseDTO send(RequestDTO request, int timeout);
}
