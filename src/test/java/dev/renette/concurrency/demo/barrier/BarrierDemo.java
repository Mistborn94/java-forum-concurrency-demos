package dev.renette.concurrency.demo.barrier;

import dev.renette.concurrency.demo.common.ConcurrencyDemo;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.renette.concurrency.demo.common.Helper.generateCallablesList;
import static dev.renette.concurrency.demo.common.Helper.log;

public class BarrierDemo extends ConcurrencyDemo {

    @Test
    void barrierDemo() throws InterruptedException {
        int tasks = 5;
        int iterations = 3;
        var barrier = new CyclicBarrier(tasks);

        executorService.invokeAll(generateCallablesList(tasks, () -> new BarrierTask(barrier, iterations)));
    }

    @AllArgsConstructor
    static class BarrierTask implements Callable<Boolean> {
        private static final AtomicInteger taskCounter = new AtomicInteger(1);

        private final int sleepTime = ThreadLocalRandom.current().nextInt(500);
        private final String name = String.format("Task #%02d", taskCounter.getAndIncrement());
        private final CyclicBarrier barrier;
        private final int iterations;

        @Override
        @SneakyThrows
        public Boolean call() {
            for (int i = 0; i < iterations; i++) {
                logSleeping(name, sleepTime);
                Thread.sleep(sleepTime);
                logWaiting(name);
                barrier.await();
                logAllowed(name);
            }
            return true;
        }
    }

    private static void logAllowed(String name) {
        log("\u001B[32m%s allowed through", name);
    }

    private static void logWaiting(String name) {
        log("\u001B[33m%s waiting for barrier", name);
    }

    private static void logSleeping(String name, int sleepTime) {
        log("\u001B[36m%s sleeping for %dms", name, sleepTime);
    }
}
