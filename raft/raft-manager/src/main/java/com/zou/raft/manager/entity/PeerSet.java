package com.zou.raft.manager.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 节点集合
 * @author zys
 * @date 2021/6/20 1:05 下午
 */
public class PeerSet {
    private List<Peer> list = new ArrayList<>();

    /**
     * leader的信息
     */
    private volatile Peer leader;

    /**
     * 自身的信息
     */
    private volatile Peer self;

    /**
     * 获取单例
     * @return
     */
    public static PeerSet getInstance() {
        return PeerSetLazyHolder.INSTANCE;
    }

    /**
     * 添加一个节点信息
     * @param peer
     * @return
     */
    public boolean addPeer(Peer peer) {
        return list.add(peer);
    }

    /**
     * 获取总的信息的一个列表
     * @return
     */
    public List<Peer> getList() {
        return list;
    }

    /**
     * 删除一个节点信息
     * @param peer
     * @return
     */
    public boolean removePeer(Peer peer) {
        return list.remove(peer);
    }

    /**
     * 单例
     */
    private static class PeerSetLazyHolder {
        private static final PeerSet INSTANCE = new PeerSet();
    }

    /**
     * 获取除自己意外的节点信息
     * @return
     */
    public List<Peer> getPeersWithOutSelf() {
        List<Peer> withOutSelfList = new ArrayList<>(list);
        withOutSelfList.remove(self);
        return withOutSelfList;
    }

    public Peer getLeader() {
        return leader;
    }

    public void setLeader(Peer leader) {
        this.leader = leader;
    }

    public Peer getSelf() {
        return self;
    }

    public void setSelf(Peer self) {
        this.self = self;
    }
}
