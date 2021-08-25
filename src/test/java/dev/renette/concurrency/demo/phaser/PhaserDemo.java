package dev.renette.concurrency.demo.phaser;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.renette.concurrency.demo.helper.Helper.generateCallablesList;
import static dev.renette.concurrency.demo.helper.Helper.log;

public class PhaserDemo {

    @Test
    void phaserDemo() throws InterruptedException {
        int tasks = 30;

        Phaser phaser = new Phaser();
        WaitingTask waitingTask = new WaitingTask(phaser);
        var callables = generateCallablesList(tasks, () -> new ArrivingTask(phaser));

        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(waitingTask);
        executorService.invokeAll(callables);

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.HOURS);
    }

    @AllArgsConstructor
    static class WaitingTask implements Runnable {
        private final Phaser phaser;

        @Override
        @SneakyThrows
        public void run() {
            int phase = 0;
            do {
                log("\u001B[31mRunnable waiting for phaser. Registered parties is %d", phaser.getRegisteredParties());
                phaser.awaitAdvance(phase);
                log("\u001B[32mRunnable allowed through. Phase %d complete", phase);
                phase += 1;
            } while (phaser.getRegisteredParties() > 0);
        }
    }

    @AllArgsConstructor
    static class ArrivingTask implements Callable<String> {

        private static final AtomicInteger taskCounter = new AtomicInteger(1);

        private final int sleepTime = ThreadLocalRandom.current().nextInt(500);
        private final int taskId = taskCounter.getAndIncrement();
        private final String name = String.format("Task #%02d", taskId);
        private final Phaser phaser;

        @Override
        @SneakyThrows
        public String call() {
            int iteration = 1;
            boolean shouldContinue = true;

            while (shouldContinue) {
                shouldContinue = taskId % (iteration + 1) == 0;
                log("\u001B[33m%s sleeping for %dms", name, sleepTime);
                Thread.sleep(sleepTime);
                if (shouldContinue) {
                    log("\u001B[36m%s arrived at phaser and waiting", name);
                    phaser.arriveAndAwaitAdvance();
                } else {
                    log("\u001B[35m%s arrived at phaser and deregistering", name);
                    phaser.arriveAndDeregister();
                }
                iteration += 1;
            }
            return name;
        }
    }
}
