package com.zou.raft.start;

import com.zou.raft.biz.INode;
import com.zou.raft.biz.impl.NodeImpl;
import com.zou.raft.manager.entity.NodeConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * @author zys
 * @date 2021/6/22 10:46 上午
 */
@Slf4j
public class RaftNodeBootStrap {
    public static void main(String[] args) throws Throwable {
        main0();
    }

    @SneakyThrows
    public static void main0() {
        String[] peerAddr = {"localhost:8775","localhost:8776","localhost:8777","localhost:8778"};

        NodeConfig nodeConfig = new NodeConfig();

        nodeConfig.setSelfPort(Integer.valueOf(System.getProperty("serverPort")));

        nodeConfig.setPeerAddrs(Arrays.asList(peerAddr));

        INode node = NodeImpl.getInstance();
        node.setConfig(nodeConfig);

        node.init();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                node.destroy();
            } catch (Throwable throwable) {
                log.error("shutdown失败 {}", throwable);
            }
        }));
    }
}
