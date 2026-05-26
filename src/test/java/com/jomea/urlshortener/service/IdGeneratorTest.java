package com.jomea.urlshortener.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class IdGeneratorTest {

    private final IdGenerator idGenerator = new IdGenerator();

    private static final Pattern BASE62_PATTERN = Pattern.compile("^[0-9A-Za-z]+$");

    @Test
    void nextId_generatesUniqueIds() {
        Set<String> ids = new java.util.HashSet<>();
        for (int i = 0; i < 1000; i++) {
            ids.add(idGenerator.nextId());
        }
        assertEquals(1000, ids.size());
    }

    @Test
    void nextId_returnsBase62String() {
        String id = idGenerator.nextId();
        assertTrue(BASE62_PATTERN.matcher(id).matches());
    }

    @Test
    void nextId_lengthInRange() {
        for (int i = 0; i < 100; i++) {
            String id = idGenerator.nextId();
            assertTrue(id.length() >= 6 && id.length() <= 12);
        }
    }

    @Test
    void nextId_concurrent_generatesUniqueIds() throws InterruptedException {
        int threads = 10;
        int idsPerThread = 100;
        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
        Thread[] workers = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            workers[i] = new Thread(() -> {
                for (int j = 0; j < idsPerThread; j++) {
                    queue.add(idGenerator.nextId());
                }
            });
            workers[i].start();
        }
        for (Thread worker : workers) {
            worker.join();
        }
        assertEquals(1000, queue.size());
        Set<String> uniqueIds = new java.util.HashSet<>(queue);
        assertEquals(1000, uniqueIds.size());
    }
}
