package com.zou.raft.biz;

import com.zou.raft.manager.entity.HeartRequestBO;
import com.zou.raft.manager.entity.HeartResponseBO;
import com.zou.raft.manager.entity.VoteRequestBO;
import com.zou.raft.manager.entity.VoteResponseBO;

/**
 * @author zys
 * @date 2021/6/19 11:02 下午
 */
public interface IConsensus {
    /**
     * rpc请求投票
     * 如果 term < currentTerm返回是false
     * 如果 voteFor为空或者就是candidateId，并且至少候选人要和自己一样新，就投了！
     * @param requestBO
     * @return
     */
    VoteResponseBO requestVote(VoteRequestBO requestBO);

    /**
     * 每次发送心跳包，附带增加日志
     * @param requestBO
     * @return
     */
    HeartResponseBO heartBeat(HeartRequestBO requestBO);
}
