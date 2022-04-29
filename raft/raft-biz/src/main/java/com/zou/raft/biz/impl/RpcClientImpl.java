package com.zou.raft.biz.impl;

import com.alipay.remoting.exception.RemotingException;
import com.zou.raft.manager.constant.RaftConstants;
import com.zou.raft.rpc.RpcClient;
import com.zou.raft.rpc.dto.RequestDTO;
import com.zou.raft.rpc.dto.ResponseDTO;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zys
 * @date 2021/6/19 11:06 下午
 */
@Slf4j
public class RpcClientImpl implements RpcClient {
    public final static com.alipay.remoting.rpc.RpcClient RPC_CLIENT = new com.alipay.remoting.rpc.RpcClient();

    static {
        RPC_CLIENT.init();
    }

    @Override
    public ResponseDTO send(RequestDTO request) {
        return send(request, RaftConstants.TIMEOUT);
    }

    @Override
    public ResponseDTO send(RequestDTO request, int timeout) {
        ResponseDTO response = null;
        try {
            response = (ResponseDTO) RPC_CLIENT.invokeSync(request.getUrl(), request, timeout);
        } catch (RemotingException e) {
            System.out.println("远程rpc异常" + e.getMessage());
            log.info("rpc RaftRemotingException", e);
        } catch (InterruptedException e) {
            System.out.println("远程rpc内部出错" + e.getMessage());
            log.info("InterruptedException", e);
        }
        return response;
    }
}
