package com.jomea.urlshortener.service;

import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Component;

@Component
public class IdGenerator {

    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final long MAX_SEQUENCE = (1L << 22) - 1;

    private final ReentrantLock lock = new ReentrantLock();
    private long lastTimestamp = -1L;
    private long sequence = 0L;

    public String nextId() {
        lock.lock();
        try {
            long timestamp = System.currentTimeMillis();

            if (timestamp < lastTimestamp) {
                while (timestamp < lastTimestamp) {
                    timestamp = System.currentTimeMillis();
                }
            }

            if (timestamp == lastTimestamp) {
                sequence = (sequence + 1) & MAX_SEQUENCE;
                if (sequence == 0) {
                    while (timestamp == lastTimestamp) {
                        timestamp = System.currentTimeMillis();
                    }
                }
            } else {
                sequence = 0L;
            }

            lastTimestamp = timestamp;

            long id = (timestamp << 22) | sequence;
            return encodeBase62(id);
        } finally {
            lock.unlock();
        }
    }

    private String encodeBase62(long value) {
        if (value == 0) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            sb.append(BASE62.charAt((int) (value % 62)));
            value /= 62;
        }
        return sb.reverse().toString();
    }
}
