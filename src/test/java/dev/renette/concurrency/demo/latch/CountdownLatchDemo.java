package dev.renette.concurrency.demo.latch;

import dev.renette.concurrency.demo.common.ConcurrencyDemo;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.renette.concurrency.demo.common.Helper.generateCallablesList;
import static dev.renette.concurrency.demo.common.Helper.log;

class CountdownLatchDemo extends ConcurrencyDemo {

    @Test
    void latchDemo() throws InterruptedException {
        int latchCount = 15;
        var countDownLatch = new CountDownLatch(latchCount);

        executorService.submit(new WaitingRunnable(countDownLatch));
        executorService.invokeAll(generateCallablesList(latchCount, () -> new CountdownCallable(countDownLatch)));
    }

    @AllArgsConstructor
    static class WaitingRunnable implements Runnable {
        private final CountDownLatch latch;

        @Override
        @SneakyThrows
        public void run() {
            logWait();
            latch.await();
            logLatchOpened();
        }
    }

    @AllArgsConstructor
    static class CountdownCallable implements Callable<Boolean> {

        private static final AtomicInteger taskCounter = new AtomicInteger(1);

        private final int sleepTime = ThreadLocalRandom.current().nextInt(500);
        private final String name = String.format("Task #%02d", taskCounter.getAndIncrement());
        private final CountDownLatch latch;

        @Override
        @SneakyThrows
        public Boolean call() {
            try {
                logSleeping(name, sleepTime);
                Thread.sleep(sleepTime);
            } finally {
                logCountingDown(name);
                latch.countDown();
                logComplete(name);
            }
            return true;
        }

    }

    private static void logComplete(String name) {
        log("\u001B[36m%s complete", name);
    }

    private static void logCountingDown(String name) {
        log("\u001B[36m%s counting down", name);
    }

    private static void logSleeping(String name, int sleepTime) {
        log("\u001B[33m%s sleeping for %sms", name, sleepTime);
    }

    private static void logLatchOpened() {
        log("\u001B[32mFinal task allowed through");
    }

    private static void logWait() {
        log("\u001B[31mFinal task waiting for latch");
    }

}
