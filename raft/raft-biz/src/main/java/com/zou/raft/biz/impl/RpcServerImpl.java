package com.zou.raft.biz.impl;

import com.alipay.remoting.BizContext;
import com.zou.raft.manager.constant.RaftConstants;
import com.zou.raft.manager.entity.ClientKVReq;
import com.zou.raft.manager.entity.HeartRequestBO;
import com.zou.raft.manager.entity.VoteRequestBO;
import com.zou.raft.rpc.RpcServer;
import com.zou.raft.rpc.dto.RequestDTO;
import com.zou.raft.rpc.dto.ResponseDTO;

/**
 * @author zys
 * @date 2021/6/19 11:19 下午
 */
public class RpcServerImpl implements RpcServer {
    private com.alipay.remoting.rpc.RpcServer rpcServer;

    private NodeImpl node;

    private volatile boolean flag;

    public RpcServerImpl(int port, NodeImpl node) {
        if (flag) {
            return;
        }
        synchronized (this) {
            if (flag) {
                return;
            }

            rpcServer = new com.alipay.remoting.rpc.RpcServer(port, false, false);
            // 注册，请求来了会跳转到handlerRequest方法
            rpcServer.registerUserProcessor(new RaftUserProcessor<RequestDTO>() {
                @Override
                public Object handleRequest(BizContext bizCtx, RequestDTO request) throws Exception {
                    System.out.println("应该到了这里了吧=======");
                    return handlerRequest(request);
                }
            });

            this.node = node;
            flag = true;
        }
    }

    @Override
    public void start() {
        rpcServer.start();
    }

    @Override
    public void shutdown() {
        rpcServer.stop();
    }

    @Override
    // todo 这里该不该弄下集群的变化呢？？？？ 工作量++ no
    public ResponseDTO handlerRequest(RequestDTO req) {
        System.out.println("///////////////// handlerRequest");
        switch (req.getCmd()) {
            case RaftConstants.REQUEST_VOTE:
                return new ResponseDTO(node.handlerVoteRequest((VoteRequestBO) req.getObj()));
            case RaftConstants.HEART_BEAT:
                return new ResponseDTO(node.handlerHeartRequest((HeartRequestBO) req.getObj()));
            case RaftConstants.CLIENT_REQ:
                return new ResponseDTO(node.handlerClientRequest((ClientKVReq) req.getObj()));
        }
        return null;
    }
}
