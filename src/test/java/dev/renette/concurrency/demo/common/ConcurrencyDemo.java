package dev.renette.concurrency.demo.common;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConcurrencyDemo {
    protected ExecutorService executorService;

    @BeforeEach
    public void init() {
        executorService = Executors.newCachedThreadPool();
    }

    @AfterEach
    public void waitForAllTasks() throws InterruptedException {
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.HOURS);
    }
}
