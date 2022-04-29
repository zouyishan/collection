package com.zys.virtualmq.remoting;

/**
 * @author zys
 * @date 2021/8/1 7:58 下午
 */
public interface RemotingService {
    public void start();

    public void shutdown();

    public void startHook();

    public void shutdownHook();
}
