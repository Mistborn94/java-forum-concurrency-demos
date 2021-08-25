package dev.renette.concurrency.demo.latch;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.renette.concurrency.demo.helper.Helper.generateCallablesList;
import static dev.renette.concurrency.demo.helper.Helper.log;

class CountdownLatchDemo {

    @Test
    void latchDemo() throws InterruptedException {
        int latchCount = 15;

        CountDownLatch countDownLatch = new CountDownLatch(latchCount);
        var finalCallable = new WaitingRunnable(countDownLatch);
        var callables = generateCallablesList(latchCount, () -> new CountdownCallable(countDownLatch));

        ExecutorService executorService = Executors.newCachedThreadPool();

        executorService.submit(finalCallable);
        executorService.invokeAll(callables);

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.HOURS);
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
                log("\u001B[33m%s sleeping for %sms", name, sleepTime);
                Thread.sleep(sleepTime);
            } finally {
                log("\u001B[36m%s counting down", name);
                latch.countDown();
                log("\u001B[36m%s complete", name);
            }
            return true;
        }
    }


    private static void logLatchOpened() {
        log("\u001B[32mFinal task allowed through");
    }

    private static void logWait() {
        log("\u001B[31mFinal task waiting for latch");
    }

}
