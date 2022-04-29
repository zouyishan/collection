package com.zou.raft.biz.impl;

import com.zou.raft.biz.IConsensus;
import com.zou.raft.manager.entity.*;
import com.zou.raft.manager.enums.NodeStatus;
import io.netty.util.internal.StringUtil;
import lombok.Data;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zys
 * @date 2021/6/19 11:21 下午
 */
@Data
public class ConsensusImpl implements IConsensus {
    public static final ReentrantLock VOTE_LOCK = new ReentrantLock();
    public static final ReentrantLock HEART_LOCK = new ReentrantLock();

    public NodeImpl node;

    public ConsensusImpl(NodeImpl node) {
        this.node = node;
    }

    @Override
    public VoteResponseBO requestVote(VoteRequestBO requestBO) {
        try {
            VoteResponseBO response = new VoteResponseBO();
            if (!VOTE_LOCK.tryLock()) {
                response.setIsVoted(false);
                response.setTerm(node.getCurrentTerm());
                System.out.println("///=/==/== 没有获取锁");
                return response;
            }

            /**
             * 当请求的任期号小于当前任期号时也是空
             */
            if (requestBO.getTerm() < node.getCurrentTerm()) {
                System.out.println("这个term 不够格");
                response.setTerm(node.getCurrentTerm());
                response.setIsVoted(false);
                return response;
            }

            if ((StringUtil.isNullOrEmpty(node.getVotedFor())) || node.getVotedFor().equals(requestBO.getCandidateId())) {
                System.out.println("这里是空就投了啊！！！！！");
                node.status = NodeStatus.FOLLOWER;
                // 设置term，投票投给谁，设置leader
                node.setCurrentTerm(requestBO.getTerm());
                node.setVotedFor(requestBO.getCandidateId());
                node.getPeerSet().setLeader(new Peer(requestBO.getCandidateId()));

                response.setIsVoted(true);
                response.setTerm(node.getCurrentTerm());
                return response;
            }

            response.setTerm(node.getCurrentTerm());
            response.setIsVoted(false);
            return response;
        } finally {
            VOTE_LOCK.unlock();
        }
    }

    /**
     * 心跳包携带附加日志
     *
     * 接受者：
     *      如果term < currentTerm 就返回false，不够格
     *      如果够格修改时间
     * @param requestBO
     * @return
     */
    @Override
    public HeartResponseBO heartBeat(HeartRequestBO requestBO) {
        HeartResponseBO heartResponseBO = new HeartResponseBO();
        heartResponseBO.setSuccess(false);
        try {
            // 上❤️锁
            if (!HEART_LOCK.tryLock()) {
                System.out.println("获取❤️锁失败");
                return heartResponseBO;
            }

            heartResponseBO.setTerm(node.getCurrentTerm());
            // 任期不够
            if (requestBO.getTerm() < node.getCurrentTerm()) {
                System.out.println("任期不足!!!!, 返回失败");
                return heartResponseBO;
            } else {
                node.status = NodeStatus.FOLLOWER;
            }

            // 更新时间, 和相应的任期
            node.setPreHeartBeatTime(System.currentTimeMillis());
            node.setPreElectionTime(System.currentTimeMillis());
            node.getPeerSet().setLeader(new Peer(requestBO.getLeaderId()));
            node.setCurrentTerm(requestBO.getTerm());

            // 心跳包，直接返回
            if (requestBO.getEntries() == null || requestBO.getEntries().length == 0) {
                heartResponseBO.setSuccess(true);
                return heartResponseBO;
            }

            if (node.getLogModule().getLastIndex() != 0 && requestBO.getPrevLogIndex() != 0) {
                LogEntryBO logEntryBO;
                if ((logEntryBO = node.getLogModule().read(requestBO.getPrevLogIndex())) != null) {
                    // 如果日志prevLogIndex位置处的日志条目的任期号和prevLogTerm 不匹配，则返回 false
                    // 减小nextIndex重试
                    if (logEntryBO.getTerm() != requestBO.getPreLogTerm()) {
                        return heartResponseBO;
                    }
                } else {
                    // index不对，需要递减nextIndex重试
                    return heartResponseBO;
                }
            }

            LogEntryBO beginLog = node.getLogModule().read(requestBO.getPrevLogIndex() + 1);
            if (beginLog != null && beginLog.getTerm() != requestBO.getEntries()[0].getTerm()) {
                // 删除这一条和之后所有的，然后写入日志和状态机
                node.getLogModule().removeOnStartIndex(requestBO.getPrevLogIndex() + 1);
            } else if (beginLog != null) {
                heartResponseBO.setSuccess(true);
                return heartResponseBO;
            }

            // todo 理应当是个异步回调。等待leader相应，返回成功后才能提交到状态机。工作量++ no
            for (LogEntryBO entry : requestBO.getEntries()) {
                node.getLogModule().write(entry);
                node.stateMachine.apply(entry);
                heartResponseBO.setSuccess(true);
            }

            if (requestBO.getLeaderCommit() > node.getCommitIndex()) {
                int commitIndex = (int) Math.min(requestBO.getLeaderCommit(), node.getLogModule().getLastIndex());
                node.setCommitIndex(commitIndex);
                node.setLastApplied(commitIndex);
            }

            heartResponseBO.setTerm(node.getCurrentTerm());
            node.status = NodeStatus.FOLLOWER;

            return heartResponseBO;
        } finally {
            HEART_LOCK.unlock();
        }
    }
}
