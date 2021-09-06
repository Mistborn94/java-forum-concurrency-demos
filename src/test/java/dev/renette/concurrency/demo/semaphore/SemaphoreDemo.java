package dev.renette.concurrency.demo.semaphore;

import dev.renette.concurrency.demo.common.ConcurrencyDemo;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.renette.concurrency.demo.common.Helper.generateCallablesList;
import static dev.renette.concurrency.demo.common.Helper.log;

public class SemaphoreDemo extends ConcurrencyDemo {

    @Test
    void semaphoreDemo() throws InterruptedException {
        int permits = 4;
        int tasks = 15;

        Semaphore semaphore = new Semaphore(permits);

        executorService.invokeAll(generateCallablesList(tasks, () -> new SemaphoreCallable(semaphore)));
    }


    @AllArgsConstructor
    static class SemaphoreCallable implements Callable<String> {

        private static final AtomicInteger taskCounter = new AtomicInteger(1);

        private final String name = String.format("Task #%02d", taskCounter.getAndIncrement());
        private final Semaphore semaphore;

        @Override
        @SneakyThrows
        public String call() {
            logWaiting(name);
            semaphore.acquire();
            try {
                logAcquired(name);
                Thread.sleep(75);
            } finally {
                logReleasing(name);
                semaphore.release();
            }
            return name;
        }
    }


    private static void logReleasing(String name) {
        log("\u001B[36m%s releasing semaphore", name);
    }

    private static void logAcquired(String name) {
        log("\u001B[32m%s acquired semaphore, sleeping for 100ms", name);
    }

    private static void logWaiting(String name) {
        log("\u001B[33m%s waiting for semaphore", name);
    }
}
