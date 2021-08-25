package dev.renette.concurrency.demo.barrier;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.renette.concurrency.demo.helper.Helper.generateCallablesList;
import static dev.renette.concurrency.demo.helper.Helper.log;

public class BarrierDemo {

    @Test
    void barrierDemo() throws InterruptedException {
        int tasks = 5;
        int iterations = 3;

        final CyclicBarrier barrier = new CyclicBarrier(tasks);
        var callables = generateCallablesList(tasks, () -> new BarrierTask(barrier, iterations));

        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.invokeAll(callables);

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.HOURS);
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
                log("\u001B[36m%s sleeping for %dms", name, sleepTime);
                Thread.sleep(sleepTime);
                log("\u001B[33m%s waiting for barrier", name);
                barrier.await();
                log("\u001B[32m%s allowed through", name);
            }
            return true;
        }
    }
}
