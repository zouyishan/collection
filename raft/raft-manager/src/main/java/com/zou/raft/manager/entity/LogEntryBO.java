package com.zou.raft.manager.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zys
 * @date 2021/6/22 9:45 下午
 */
@Data
public class LogEntryBO implements Serializable, Comparable<LogEntryBO> {
    private Long index;
    private Long term;
    private Command command;

    @Override
    public int compareTo(LogEntryBO o) {
        if (o == null) {
            return -1;
        }
        return this.getIndex() > o.getIndex() ? 1 : -1;
    }
}
