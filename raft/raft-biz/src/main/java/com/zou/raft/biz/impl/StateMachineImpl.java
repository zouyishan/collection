package com.zou.raft.biz.impl;

import com.alibaba.fastjson.JSON;
import com.zou.raft.biz.IStateMachine;
import com.zou.raft.manager.entity.Command;
import com.zou.raft.manager.entity.LogEntryBO;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.File;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author zys
 * @date 2021/6/24 7:11 下午
 */
@Slf4j
public class StateMachineImpl implements IStateMachine {
    private static String dbDir = null;
    private static String stateMachineDir = null;
    private static RocksDB MACHINE_DB = null;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    static {
        dbDir = "./rocksDB-raft/" + System.getProperty("serverPort");
        stateMachineDir = dbDir + "/stateMachine";
        RocksDB.loadLibrary();
    }

    private StateMachineImpl() {
        Options options = new Options();
        options.setCreateIfMissing(true);

        File file = new File(stateMachineDir);
        boolean success = false;
        if (!file.exists()) {
            success = file.mkdirs();
        }
        if (success) {
            log.info("新建了一个文件： " + stateMachineDir);
            System.out.println("新建一个文件: " + stateMachineDir);
        }
        try {
            MACHINE_DB = RocksDB.open(options, stateMachineDir);
        } catch (RocksDBException e) {
            log.error("打开rocksDB失败：{}", e.getMessage());
            System.out.println("打开文件失败: " + e.getMessage());
        }
    }

    public static StateMachineImpl getInstance() {
        return StateMachineImplLazyHolder.INSTANCE;
    }

    private static class StateMachineImplLazyHolder {
        private static final StateMachineImpl INSTANCE = new StateMachineImpl();
    }

    @Override
    public void apply(LogEntryBO logEntryBO) {
        try {
            Command command = logEntryBO.getCommand();
            if (command == null) {
                throw new IllegalArgumentException("command can not be null, logEntry : " + logEntryBO.toString());
            }
            String key = command.getKey();
            MACHINE_DB.put(key.getBytes(), JSON.toJSONBytes(logEntryBO));
        } catch (Exception e) {
            System.out.println("状态机apply异常");
        }
    }

    @Override
    public LogEntryBO get(String key) {
        try {
            byte[] bytes = MACHINE_DB.get(key.getBytes());
            if (bytes == null) {
                return null;
            }
            return JSON.parseObject(bytes, LogEntryBO.class);
        } catch (RocksDBException e) {
            log.info(e.getMessage());
        }
        return null;
    }

    @Override
    public String getString(String key) {
        try {
            byte[] bytes = MACHINE_DB.get(key.getBytes());
            if (bytes != null) {
                return new String(bytes);
            }
        } catch (RocksDBException e) {
            log.info(e.getMessage());
            System.out.println("getString error" + e.getMessage());
        }
        return null;
    }

    @Override
    public void setString(String key, String value) {
        try {
            MACHINE_DB.put(key.getBytes(), value.getBytes());
        } catch (RocksDBException e) {
            log.info(e.getMessage());
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void delString(String... key) {
        try {
            for (String s : key) {
                MACHINE_DB.delete(s.getBytes());
            }
        } catch (RocksDBException e) {
            log.info(e.getMessage());
            System.out.println(e.getMessage());
        }
    }
}
