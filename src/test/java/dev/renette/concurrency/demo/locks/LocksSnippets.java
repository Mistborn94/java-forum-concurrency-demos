package dev.renette.concurrency.demo.locks;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static dev.renette.concurrency.demo.common.Helper.generateCallablesList;
import static dev.renette.concurrency.demo.common.Helper.log;

public class LocksSnippets {

    @Test
    void locksDemo() throws InterruptedException {
        Lock lock = new ReentrantLock();
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.invokeAll(generateCallablesList(15, () -> new LockingLoop(lock)));

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.HOURS);
    }

    @AllArgsConstructor
    static class LockingLoop implements Callable<Boolean> {
        private static final AtomicInteger counter = new AtomicInteger(0);
        private final String name = "LockingLoop #" + counter.getAndIncrement();

        private final int sleepTime = ThreadLocalRandom.current().nextInt(500);
        private final Lock lock;

        @Override
        @SneakyThrows
        public Boolean call() {

            try {
                lock.lock();
                doSomething();
            } finally {
                lock.unlock();
            }


            if (lock.tryLock()) {

                try {
                    doSomething();
                } finally {
                    lock.unlock();
                }
            } else {
                //Unable to acquire lock
                doSomethingElse();
            }


            return true;
        }

        private void doSomethingElse() {

        }

        private void doSomething() {

        }
    }


    private static void logComplete(String name) {
        log("\u001B[32m%s complete", name);
    }

    private static void logLockAcquired(String name, int sleepTime) {
        log("\u001B[36m%s lock Acquired. Sleeping for %sms", name, sleepTime);
    }

    private static void logWaiting(String name) {
        log("\u001B[33m%s waiting for the lock", name);
    }
}
