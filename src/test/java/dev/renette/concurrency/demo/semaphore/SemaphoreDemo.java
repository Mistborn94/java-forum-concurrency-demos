package dev.renette.concurrency.demo.semaphore;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.renette.concurrency.demo.helper.Helper.generateCallablesList;
import static dev.renette.concurrency.demo.helper.Helper.log;

public class SemaphoreDemo {


    @Test
    void semaphoreDemo() throws InterruptedException {
        int permits = 2;
        int tasks = 15;

        Semaphore semaphore = new Semaphore(permits);
        var callables = generateCallablesList(tasks, () -> new SemaphoreCallable(semaphore));

        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.invokeAll(callables);

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.HOURS);
    }


    @AllArgsConstructor
    static class SemaphoreCallable implements Callable<String> {

        private static final AtomicInteger taskCounter = new AtomicInteger(1);

        private final String name = String.format("Task #%02d", taskCounter.getAndIncrement());
        private final Semaphore semaphore;

        @Override
        @SneakyThrows
        public String call() {
            log("\u001B[33m%s waiting for semaphore", name);
            semaphore.acquire();
            try {
                log("\u001B[32m%s acquired semaphore, sleeping for 100ms", name);
                Thread.sleep(100);
            } finally {
                log("\u001B[36m%s releasing semaphore", name);
                semaphore.release();
            }
            return name;
        }
    }
}
