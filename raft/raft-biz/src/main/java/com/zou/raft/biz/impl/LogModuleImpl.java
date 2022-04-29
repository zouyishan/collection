package com.zou.raft.biz.impl;

import com.zou.raft.biz.ILogModule;
import com.zou.raft.manager.constant.RaftConstants;
import com.zou.raft.manager.entity.LogEntryBO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import com.alibaba.fastjson.JSON;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author zys
 * @date 2021/6/24 7:11 下午
 */
@Data
@Slf4j
public class LogModuleImpl implements ILogModule {
    private static String dbDir = null;
    private static String logsDir = null;
    private static RocksDB LOG_DB = null;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    static {
        dbDir = "./rocksDB-raft/" + System.getProperty("serverPort");
        logsDir = dbDir + "/logModule";
        RocksDB.loadLibrary();
    }

    private LogModuleImpl() {
        Options options = new Options();
        options.setCreateIfMissing(true);

        File file = new File(logsDir);
        boolean success = false;
        if (!file.exists()) {
            success = file.mkdirs();
        }
        if (success) {
            log.info("新建了一个文件： " + logsDir);
            System.out.println("新建一个文件: " + logsDir);
        }
        try {
            LOG_DB = RocksDB.open(options, logsDir);
        } catch (RocksDBException e) {
            log.error("打开rocksDB失败：{}", e.getMessage());
            System.out.println("打开文件失败: " + e.getMessage());
        }
    }

    public static LogModuleImpl getInstance() {
        return LogModuleImplHolder.INSTANCE;
    }

    private static class LogModuleImplHolder {
        private static final LogModuleImpl INSTANCE = new LogModuleImpl();
    }

    @Override
    public void write(LogEntryBO logEntryBO) {
        try {
            boolean success = false;
            lock.writeLock().lockInterruptibly();
            try {
                logEntryBO.setIndex(getLastIndex() + 1);
                LOG_DB.put(logEntryBO.getIndex().toString().getBytes(), JSON.toJSONBytes(logEntryBO));
                success = true;
                System.out.println("写入成功：logEntry info: " + logEntryBO);
            } finally {
                if (success) {
                    updateLastIndex(logEntryBO.getIndex());
                }
                lock.writeLock().unlock();
            }
        } catch (Exception e) {
            System.out.println("rocksDB写出错: " + e.getMessage());
        }
    }

    @Override
    public LogEntryBO read(Long index) {
        try {
            byte[] result = LOG_DB.get(convert(index));
            if (result == null) {
                return null;
            }
            return JSON.parseObject(result, LogEntryBO.class);
        } catch (RocksDBException e) {
            System.out.println("rocksDB读异常");
            log.warn(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void removeOnStartIndex(Long startIndex) {
        try {
            boolean success = false;
            // 其实这里不用原子类，已经加过锁了。
            AtomicInteger count = new AtomicInteger(0);
            lock.writeLock().lockInterruptibly();
            try {
                for (long i = startIndex; i <= getLastIndex(); i++) {
                    LOG_DB.delete(String.valueOf(i).getBytes());
                    count.incrementAndGet();
                }
                success = true;
                System.out.println("删除成功, count: " + count + " startIndex: " + startIndex + " lastIndex: " + getLastIndex());
            } finally {
                if (success) {
                    updateLastIndex(getLastIndex() - count.get());
                }
                lock.writeLock().unlock();
            }
        } catch (Exception e) {
            System.out.println("rocksDB写出错: " + e.getMessage());
        }
    }

    @Override
    public LogEntryBO getLast() {
        try {
            byte[] result = LOG_DB.get(convert(getLastIndex()));
            if (result == null) {
                return null;
            }
            return JSON.parseObject(result, LogEntryBO.class);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Long getLastIndex() {
        byte[] lastIndex = "-1".getBytes();
        try {
            lastIndex = LOG_DB.get(RaftConstants.LAST_INDEX_KEY);
            if (lastIndex == null) {
                lastIndex = "-1".getBytes();
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return Long.valueOf(new String(lastIndex));
    }

    private byte[] convert(Long key) {
        return String.valueOf(key).getBytes();
    }

    // 执行此时 是上锁的
    private void updateLastIndex(Long index) {
        try {
            LOG_DB.put(RaftConstants.LAST_INDEX_KEY, index.toString().getBytes());
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }
}
