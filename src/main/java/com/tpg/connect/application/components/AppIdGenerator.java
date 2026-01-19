package com.tpg.connect.application.components;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class AppIdGenerator {

    private static final long CUSTOM_EPOCH = 1704067200000L;

    private static final int NODE_ID_BITS = 10;
    private static final int SEQUENCE_BITS = 12;

    private static final long MAX_NODE_ID = (1L << NODE_ID_BITS) - 1;
    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;

    private static final long NODE_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + NODE_ID_BITS;

    private static final String APP_ID_PREFIX = "APP-";

    private final long nodeId;
    private final AtomicLong sequence = new AtomicLong(0);
    private volatile long lastTimestamp = -1L;

    public AppIdGenerator() {
        this.nodeId = 1L;
    }

    public AppIdGenerator(long nodeId) {
        if (nodeId < 0 || nodeId > MAX_NODE_ID)
            throw new IllegalArgumentException("Node ID must be between 0 and " + MAX_NODE_ID);
        this.nodeId = nodeId;
    }

    public synchronized String generateAppId() {
        long id = generateId();
        return APP_ID_PREFIX + id;
    }

    private long generateId() {
        long currentTimestamp = currentTimestamp();
        if (currentTimestamp <= lastTimestamp) {
            long seq = sequence.incrementAndGet() & MAX_SEQUENCE;
            if (seq == 0)
                lastTimestamp++;
        } else {
            lastTimestamp = currentTimestamp;
            sequence.set(0);
        }
        return ((lastTimestamp - CUSTOM_EPOCH) << TIMESTAMP_SHIFT)
                | (nodeId << NODE_ID_SHIFT)
                | sequence.get();
    }

    private long currentTimestamp() {
        return System.currentTimeMillis();
    }
}

