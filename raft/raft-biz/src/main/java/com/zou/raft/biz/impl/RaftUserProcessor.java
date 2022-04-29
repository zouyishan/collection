package com.zou.raft.biz.impl;

import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.AbstractUserProcessor;
import com.zou.raft.manager.exception.RaftNotSupportException;
import com.zou.raft.rpc.dto.RequestDTO;

/**
 * @author zys
 * @date 2021/6/22 2:41 下午
 */
public abstract class RaftUserProcessor<T> extends AbstractUserProcessor<T> {
    @Override
    public void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, T request) {
        throw new RaftNotSupportException(
                "Raft Server not support handleRequest(BizContext bizCtx, AsyncContext asyncCtx, T request) ");
    }


    @Override
    public String interest() {
        return RequestDTO.class.getName();
    }
}
