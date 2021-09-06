package dev.renette.concurrency.demo.locks;

import dev.renette.concurrency.demo.common.ConcurrencyDemo;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static dev.renette.concurrency.demo.common.Helper.generateCallablesList;
import static dev.renette.concurrency.demo.common.Helper.log;

public class LocksDemo extends ConcurrencyDemo {

    @Test
    void locksDemo() throws InterruptedException {
        int tasks = 15;
        Lock lock = new ReentrantLock();
        executorService.invokeAll(generateCallablesList(tasks, () -> new LockingLoop(lock)));
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
            logWaiting(name);
            if (lock.tryLock()) {
//            lock.lock();
                try {
                    logLockAcquired(name, sleepTime);
                    Thread.sleep(sleepTime);
                    logComplete(name);
                } finally {
                    lock.unlock();
                }
            } else {
                log("%s Doing nothing", name);
            }
            return true;
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
