package com.zou.raft.biz.impl;

import com.zou.raft.biz.ILogModule;
import com.zou.raft.biz.INode;
import com.zou.raft.biz.IStateMachine;
import com.zou.raft.biz.LifeCycle;
import com.zou.raft.manager.constant.RaftConstants;
import com.zou.raft.manager.entity.*;
import com.zou.raft.manager.enums.NodeStatus;
import com.zou.raft.manager.utils.RaftThreadPool;
import com.zou.raft.rpc.RpcClient;
import com.zou.raft.rpc.RpcServer;
import com.zou.raft.rpc.dto.RequestDTO;
import com.zou.raft.rpc.dto.ResponseDTO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * 节点
 *
 * @author zys
 * @date 2021/6/19 11:21 下午
 */
@Slf4j
@Data
// 真的太恶心了=== 居然写出了这么恶心的代码===
public class NodeImpl implements INode, LifeCycle {
    /**
     * 选举的间隔时间
     */
    public volatile long electionTime = 15 * 1000;

    /**
     * 上次选举时间
     */
    private volatile long preElectionTime;

    /**
     * 当前节点状态
     */
    public volatile NodeStatus status = NodeStatus.FOLLOWER;

    /**
     * 接受请求
     */
    public static RpcServer RPC_SERVER;

    /**
     * 本地rpc，用于leader发心跳包，选举用来发请求投票
     */
    public RpcClient rpcClient = new RpcClientImpl();

    /**
     * 当前获得选票候选人的ID
     */
    public volatile String votedFor;

    /**
     * 一致性模块
     */
    private ConsensusImpl consensus;

    /**
     * 当前的任期
     */
    private volatile Long currentTerm = 0L;

    /**
     * 上次的心跳时间
     */
    private volatile long preHeartBeatTime;

    /**
     * 心跳的间隔基数
     */
    public final long heartBeatTick = 3000;

    /**
     * 日志，状态机
     */
    ILogModule logModule;

    IStateMachine stateMachine;

    /**
     * 基本配置
     */
    private PeerSet peerSet;
    private NodeConfig config;

    /**
     * 定时任务线程
     */
    private HeartBeatTask heartBeatTask = new HeartBeatTask();
    private ElectionTask electionTask = new ElectionTask();
    public volatile boolean started;

    public static NodeImpl getInstance() {
        return DefaultNodeLazyHolder.INSTANCE;
    }

    private static class DefaultNodeLazyHolder {
        private static final NodeImpl INSTANCE = new NodeImpl();
    }

    /** 已知的最大的已经被提交的日志条目的索引值 */
    volatile long commitIndex;

    /** 最后被应用到状态机的日志条目索引值（初始化为 0，持续递增) */
    volatile long lastApplied = 0;

    /** 对于每一个服务器，需要发送给他的下一个日志条目的索引值（初始化为领导人最后索引值加一） */
    Map<Peer, Long> nextIndexs;

    /** 对于每一个服务器，已经复制给他的日志的最高索引值 */
    Map<Peer, Long> matchIndexs;

    @Override
    public void init() throws Throwable {
        if (started) {
            return;
        }
        synchronized (this) {
            if (started) {
                return;
            }
            RPC_SERVER.start();

            consensus = new ConsensusImpl(this);
            RaftThreadPool.scheduleWithFixedDelay(heartBeatTask, 500);
            RaftThreadPool.scheduleAtFixedRate(electionTask, 6000, 500);

            started = true;
            System.out.println("start success selfId: {}" + peerSet.getSelf());
            log.info("start success, selfId: {}", peerSet.getSelf());
        }
    }

    @Override
    public void destroy() throws Throwable {
        RPC_SERVER.shutdown();
    }

    @Override
    public void setConfig(NodeConfig config) {
        this.config = config;
        peerSet = PeerSet.getInstance();
        logModule = LogModuleImpl.getInstance();
        stateMachine = StateMachineImpl.getInstance();
        for (String s : config.getPeerAddrs()) {
            Peer peer = new Peer(s);
            peerSet.addPeer(peer);
            if (s.equals("localhost:" + config.getSelfPort())) {
                peerSet.setSelf(peer);
            }
        }
        RPC_SERVER = new RpcServerImpl(config.getSelfPort(), this);
    }

    @Override
    public VoteResponseBO handlerVoteRequest(VoteRequestBO req) {
        log.info("voteRequest {}", req);
        System.out.println("请求投票 + " + req);
        return consensus.requestVote(req);
    }

    /**
     * 请求日志复制
     * @param req
     * @return
     */
    @Override
    public HeartResponseBO handlerAppendEntries(HeartRequestBO req) {
        if (req.getEntries() != null) {
            System.out.println("日志要复制的内容：" + req.getEntries());
        }
        return consensus.heartBeat(req);
    }

    /**
     * 分发日志，这里重试需要吗？
     * @param peer
     * @param logEntryBO
     * @return
     */
    public Future<Boolean> replication(Peer peer, LogEntryBO logEntryBO) {
        return RaftThreadPool.submit(() -> {
            long start = System.currentTimeMillis(), end = start;
            while ((end - start) < 20 * 1000L) {
                HeartRequestBO heartRequestBO = new HeartRequestBO();
                heartRequestBO.setTerm(currentTerm);
                heartRequestBO.setServerId(peer.getAddr());
                heartRequestBO.setLeaderId(peerSet.getSelf().getAddr());

                // 需要发送给他的下一个日志条目的索引值
                heartRequestBO.setLeaderCommit(commitIndex);
                Long nextIndex = nextIndexs.get(peer);
                LinkedList<LogEntryBO> logEntryList = new LinkedList<>();
                if (logEntryBO.getIndex() >= nextIndex) {
                    for (long i = nextIndex; i <= logEntryBO.getIndex(); i++) {
                        LogEntryBO read = logModule.read(i);
                        if (read != null) {
                            logEntryList.add(read);
                        }
                    }
                } else {
                    logEntryList.add(logEntryBO);
                }

                // 获取最小日志的前一个
                LogEntryBO preLog = getPreLog(logEntryList.getFirst());
                heartRequestBO.setPreLogTerm(preLog.getTerm());
                heartRequestBO.setPrevLogIndex(preLog.getIndex());
                heartRequestBO.setEntries(logEntryList.toArray(new LogEntryBO[0]));

                /** 请求构建完成 **/
                RequestDTO request = new RequestDTO<>();
                request.setUrl(peer.getAddr());
                request.setObj(heartRequestBO);
                request.setCmd(RaftConstants.HEART_BEAT);

                try {
                    ResponseDTO response = getRpcClient().send(request);
                    if (response == null) {
                        return false;
                    }
                    HeartResponseBO result = (HeartResponseBO) response.getResult();

                    if (result != null && result.getSuccess()) {
                        System.out.println("更新成功！！！！！！");
                        nextIndexs.put(peer, logEntryBO.getIndex() + 1);
                        matchIndexs.put(peer, logEntryBO.getIndex());
                        return true;
                    } else if (result != null) {
                        // 对方比我大
                        if (result.getTerm() > currentTerm) {
                            currentTerm = result.getTerm();
                            status = NodeStatus.FOLLOWER;
                            return false;
                        } else {
                            // 没我大，但是失败了，说明index对不上
                            if (nextIndex == 0) {
                                nextIndex = 1L;
                            }
                            nextIndexs.put(peer, nextIndex - 1);
                        }
                    }

                    end = System.currentTimeMillis();
                } catch (Exception e) {
                    // todo 重试？？？？？？？？ 工作量++ no
                    System.out.println(e.getMessage());
                    return false;
                }
            }
            // 超时
            return false;
        });
    }

    /**
     * 获取前一个元素
     * @param logEntryBO
     * @return
     */
    private LogEntryBO getPreLog(LogEntryBO logEntryBO) {
        LogEntryBO read = logModule.read(logEntryBO.getIndex() - 1);
        if (read == null) {
            read.setIndex(0L);
            read.setTerm(0L);
            read.setCommand(null);
        }
        return read;
    }

    /**
     * 主leader处理来自客户端的请求。
     *
     * 思路：本地预提交，发送状态机，异步等待回复结果，多数赞同提交，失败则回滚。直接把我带走 :-)
     * @param req
     * @return
     */
    @Override
    // 特别难搞=_=,   直接被带走:-)
    public synchronized ClientKVAck handlerClientRequest(ClientKVReq req) {
        if (status != NodeStatus.LEADER) {
            System.out.println("不是leader，调用跳转方法：leader addr" + peerSet.getLeader().getAddr() + " myaddr: " + peerSet.getSelf().getAddr());
            return redirect(req);
        }

        LogEntryBO logEntryBO = new LogEntryBO();
        logEntryBO.setTerm(currentTerm);
        Command command = new Command();
        command.setKey(req.getKey());
        command.setValue(req.getValue());
        logEntryBO.setCommand(command);

        /**
         * 预提交日志在node上
         */
        logModule.write(logEntryBO);
        System.out.println("日志预写：" + logEntryBO + " index: " + logEntryBO.getIndex());

        // 日志分发复制，此处并发度较高
        AtomicInteger success = new AtomicInteger(0);
        List<Future<Boolean>> futureList = new CopyOnWriteArrayList<>();
        int count = 0;
        for (Peer peer : peerSet.getPeersWithOutSelf()) {
            count++;
            // 并发rpc复制
            futureList.add(replication(peer, logEntryBO));
        }

        CountDownLatch countDownLatch = new CountDownLatch(futureList.size());
        List<Boolean> resultList = new CopyOnWriteArrayList<>();

        getRPCHeartResult(futureList, countDownLatch, resultList);

        try {
            countDownLatch.await(4000, MILLISECONDS);
        } catch (Exception e) {
            // 失败打印堆栈
            e.printStackTrace();
        }

        for (Boolean bool : resultList) {
            if (bool) {
                success.incrementAndGet();
            }
        }

        // 已经复制给他的日志的最高索引值
        ArrayList<Long> matchIndexList = new ArrayList<>(matchIndexs.values());

        /**
         * 如果存在一个满足 N > commitIndex的 N，并且大多数的matchIndex[i] >= N成立
         * 并且logBO[N].term == currentTerm 成立，那么令 commitIndex 等于这个N
         */
        int median = 0;
        if (matchIndexList.size() >= 2) {
            Collections.sort(matchIndexList);
            median = matchIndexList.size() / 2;
        }
        Long N = matchIndexList.get(median);
        if (N > commitIndex) {
            LogEntryBO entry = logModule.read(N);
            if (entry != null && entry.getTerm() == currentTerm) {
                commitIndex = N;
            }
        }

        if (success.get() >= (count / 2)) {
            commitIndex = logEntryBO.getIndex();
            /**
             * 应用到状态机中
             */
            getStateMachine().apply(logEntryBO);
            lastApplied = commitIndex;

            System.out.println("成功应用状态机");
            return new ClientKVAck("ok");
        } else {
            // 回滚已经提交的日志
            logModule.removeOnStartIndex(logEntryBO.getIndex());
            return new ClientKVAck("fail");
        }
    }

    private void getRPCHeartResult(List<Future<Boolean>> futureList, CountDownLatch latch, List<Boolean> resultList) {
        for (Future<Boolean> future : futureList) {
            RaftThreadPool.execute(() -> {
                try {
                    resultList.add(future.get(3000, MILLISECONDS));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    resultList.add(false);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    resultList.add(false);
                } catch (TimeoutException e) {
                    e.printStackTrace();
                    resultList.add(false);
                } finally {
                    latch.countDown();
                }
            });
        }
    }
    /**
     * 跳转请求
     * @param req
     * @return
     */
    @Override
    public ClientKVAck redirect(ClientKVReq req) {
        RequestDTO<ClientKVReq> request = new RequestDTO<>();
        request.setUrl(peerSet.getLeader().getAddr());
        request.setCmd(RaftConstants.CLIENT_REQ);
        request.setObj(req);
        ResponseDTO response = rpcClient.send(request);
        return (ClientKVAck) response.getResult();
    }

    public HeartResponseBO handlerHeartRequest(HeartRequestBO req) {
        System.out.println("发送心跳包来了 + " + req);
        return consensus.heartBeat(req);
    }

    /**
     * 选举线程
     * 1。 在转变成候选人后就立即开始选举过程
     *      自增当前任期号
     *      给自己投票
     *      重置选举超时计时器
     *      发送rpc请求给其他服务器
     * 2。如果接受到大多数服务器的投票，那就变成领导人
     * 3。如果接受到来自新的领导人的附加日志 RPC，那就转换成跟随者
     * 4。如果选举过程超时，那么就再发起一轮选举
     */
    private class ElectionTask implements Runnable {
        @Override
        public void run() {
            System.out.println("选举线程 开始 status: " + status);
            System.out.println("term " + currentTerm);
            if (status == NodeStatus.LEADER) {
                return;
            }

            long current = System.currentTimeMillis();
            electionTime = electionTime + ThreadLocalRandom.current().nextInt(50);

            System.out.println(current + " " + preElectionTime + " " + electionTime);
            if (current - preElectionTime < electionTime) {
                return;
            }
            status = NodeStatus.CANDIDATE;
            log.info("node {} become CANDIDATE", peerSet.getSelf());
            System.out.println("node become candidate" + peerSet.getSelf());

            preElectionTime = System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(200) + 150;

            /**
             * 任期 +1， 投票给自己
             */
            currentTerm = currentTerm + 1;
            votedFor = peerSet.getSelf().getAddr();

            List<Peer> peers = peerSet.getPeersWithOutSelf();
            ArrayList<Future> futureArrayList = new ArrayList<>();

            log.info("peerList size: {}, peer list content: {}", peers.size(), peers);
            System.out.println("peerlist size: peer list content: " + peers.size() + peers);

            /**
             * 异步获取返回结果
             */
            for (Peer peer : peers) {
                futureArrayList.add(RaftThreadPool.submit(() -> {
                    long lastTerm = 0L;

                    VoteRequestBO voteRequestBO = new VoteRequestBO();
                    voteRequestBO.setTerm(currentTerm);
                    voteRequestBO.setCandidateId(peerSet.getSelf().getAddr());

                    RequestDTO<VoteRequestBO> requestDTO = new RequestDTO<>();
                    requestDTO.setObj(voteRequestBO);
                    requestDTO.setCmd(RaftConstants.REQUEST_VOTE);
                    requestDTO.setUrl(peer.getAddr());

                    try {
                        ResponseDTO<VoteResponseBO> responseDTO = getRpcClient().send(requestDTO);
                        System.out.println("这里的responseDTO是。。。。。" + responseDTO);
                        return responseDTO;
                    } catch (Exception e) {
                        log.error("选举请求RPC请求失败: {}", requestDTO.getUrl());
                        System.out.println("请求RPC失败: " + requestDTO.getUrl());
                        return null;
                    }
                }));
            }

            AtomicInteger successCount = new AtomicInteger(0);
            CountDownLatch countDownLatch = new CountDownLatch(futureArrayList.size());

            log.info("异步回调结果的大小：futureArrayList.size(): {}", futureArrayList.size());
            System.out.println("异步回调结果大小为：" + futureArrayList.size());
            /**
             * 投票阶段
             */
            for (Future future : futureArrayList) {
                RaftThreadPool.submit(() -> {
                    try {
                        ResponseDTO<VoteResponseBO> responseDTO = (ResponseDTO<VoteResponseBO>) future.get(5000, MILLISECONDS);
                        if (responseDTO == null) {
                            log.info("没有投票成功 responseDTO == null");
                            System.out.println("没有投票成功:" + responseDTO);
                            return -1;
                        }

                        /**
                         * 请求的是否投票成功
                         */
                        boolean isVoteSuccess = responseDTO.getResult().getIsVoted();

                        if (isVoteSuccess) {
                            successCount.incrementAndGet();
                        } else {
                            long resTerm = responseDTO.getResult().getTerm();
                            if (resTerm >= currentTerm) {
                                currentTerm = resTerm;
                            }
                        }

                        return 0;
                    } catch (Exception e) {
                        log.error("异步回调: future.get exception: e: {}", e.getMessage());
                        System.out.println("异步回调错误：" + e.getMessage());
                        return -1;
                    } finally {
                        countDownLatch.countDown();
                    }
                });
            }

            try {
                // 如果超时就直接重新搞吧
                countDownLatch.await(3500, MILLISECONDS);
            } catch (Exception e) {
                log.error("等待过程中有问题：countDownLatch, e {}", e.getMessage());
                System.out.println("等待过程中有问题：countDownLatch, e {}" + e.getMessage());
            }

            // 加上自己
            int totalCount = successCount.get();
            log.info("node {} 可能变成leader！！！， totalCount = {}, status: {}", peerSet.getSelf(), totalCount, status);
            System.out.println("node 可能变成leader: " + peerSet.getSelf());
            System.out.println("======== " + totalCount);

            // 有状态改变，成为follower就不能当选
            if (status == NodeStatus.FOLLOWER) {
                return;
            }

            if (totalCount >= peers.size() / 2) {
                log.warn("node {} become leader", peerSet.getSelf());
                System.out.println("node become leader" + peerSet.getSelf());
                status = NodeStatus.LEADER;
                peerSet.setLeader(peerSet.getSelf());
                votedFor = "";
                // todo 成为主leader后该干什么
            } else {
                // 重新选举
                System.out.println("选举失败，从新选举：" + peerSet.getSelf());
                votedFor = "";
            }
        }
    }

    private class HeartBeatTask implements Runnable {
        @Override
        public void run() {
            System.out.println("心跳线程开始  状态" + status);
            if (status != NodeStatus.LEADER) {
                return;
            }

            // 间歇时间
            long current = System.currentTimeMillis();
            if (current - preHeartBeatTime < heartBeatTick) {
                return;
            }

            log.info("========= HeartBeat Start =========");
            System.out.println("心跳开始");
            for (Peer peer : peerSet.getPeersWithOutSelf()) {
                log.info("Peer info: {} ", peer);
                System.out.println("peer的信息  " + peer);
            }

            preHeartBeatTime = System.currentTimeMillis();

            for (Peer peer : peerSet.getPeersWithOutSelf()) {
                HeartRequestBO heartRequestBO = new HeartRequestBO();
                heartRequestBO.setLeaderId(peerSet.getSelf().getAddr());
                heartRequestBO.setServerId(peer.getAddr());
                heartRequestBO.setTerm(currentTerm);

                RequestDTO<HeartRequestBO> requestDTO = new RequestDTO<>();
                requestDTO.setUrl(heartRequestBO.getServerId());
                requestDTO.setCmd(RaftConstants.HEART_BEAT);
                requestDTO.setObj(heartRequestBO);

                // 往线程池里面丢任务
                RaftThreadPool.execute(() -> {
                    try {
                        System.out.println("心跳的请求参数：" + requestDTO);
                        ResponseDTO<HeartResponseBO> response = getRpcClient().send(requestDTO);
                        HeartResponseBO result = response.getResult();
                        long term = result.getTerm();

                        /**
                         * 当前任期号没从节点新，更新term，votedFor为空，状态变为FOLLOWER
                         */
                        if (term > currentTerm) {
                            log.error("self will become follower, he's term : {}, my term : {}", term, currentTerm);
                            System.out.println("leader变成follower" + term);
                            currentTerm = term;
                            votedFor = "";
                            status = NodeStatus.FOLLOWER;
                        }
                    } catch (Exception e) {
                        log.error("HeartBeatTask RPC Fail, request URL : {} ", requestDTO.getUrl());
                        System.out.println("心跳发送失败" + requestDTO.getUrl());
                    }
                });
            }
        }
    }
}
