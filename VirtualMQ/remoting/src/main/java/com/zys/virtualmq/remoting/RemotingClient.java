package com.zys.virtualmq.remoting;

/**
 * @author zys
 * @date 2021/8/1 7:57 下午
 */
public interface RemotingClient extends RemotingService{
    void syncSendMsg();

    void asyncSendMsg();
}