package com.zou.raft.biz;

import com.zou.raft.manager.entity.*;

/**
 * 抽象出来的节点
 * @author zys
 * @date 2021/6/19 11:21 下午
 */
public interface INode extends LifeCycle {
    /**
     * 设置配置文件
     *
     * @param config
     */
    void setConfig(NodeConfig config);

    /**
     * 处理投票请求
     *
     * @param req
     * @return
     */
    VoteResponseBO handlerVoteRequest(VoteRequestBO req);

    /**
     * 处理附加日志
     * @param req
     * @return
     */
    HeartResponseBO handlerAppendEntries(HeartRequestBO req);

    /**
     * leader 处理来自客户端的请求
     * @param req
     * @return
     */
    ClientKVAck handlerClientRequest(ClientKVReq req);

    /**
     * 如果是请求发送到非主上，则跳转请求到leader上
     * @param req
     * @return
     */
    ClientKVAck redirect(ClientKVReq req);
}
