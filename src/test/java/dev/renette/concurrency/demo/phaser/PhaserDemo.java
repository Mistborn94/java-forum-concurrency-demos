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
                logWaiting(phaser.getRegisteredParties());
                phaser.awaitAdvance(phase);
                logPhaseComplete(phase);
                phase += 1;
            } while (phaser.getRegisteredParties() > 0);
        }
    }

    @AllArgsConstructor
    static class ArrivingTask implements Callable<String> {

        private static final AtomicInteger taskCounter = new AtomicInteger(1);

        private final int taskId = taskCounter.getAndIncrement();
        private final String name = String.format("Task #%02d", taskId);
        private final int sleepTime = ThreadLocalRandom.current().nextInt(200, 500);

        private final Phaser phaser;

        @Override
        @SneakyThrows
        public String call() {
            int iteration = 1;
            boolean shouldContinue = true;

            while (shouldContinue) {
                shouldContinue = taskId % (iteration + 1) == 0;
                logSleeping(name, sleepTime);
                Thread.sleep(sleepTime);
                if (shouldContinue) {
                    logWaiting(name);
                    phaser.arriveAndAwaitAdvance();
                } else {
                    logDeregistering(name);
                    phaser.arriveAndDeregister();
                }
                iteration += 1;
            }
            return name;
        }
    }


    private static void logPhaseComplete(int phase) {
        log("\u001B[32mRunnable allowed through. Phase %d complete", phase);
    }

    private static void logWaiting(int partyCount) {
        log("\u001B[31mRunnable waiting for phaser. Registered parties count is %d", partyCount);
    }

    private static void logDeregistering(String name) {
        log("\u001B[35m%s arrived at phaser and deregistering", name);
    }

    private static void logWaiting(String name) {
        log("\u001B[36m%s arrived at phaser and waiting", name);
    }

    private static void logSleeping(String name, int sleepTime) {
        log("\u001B[33m%s sleeping for %dms", name, sleepTime);
    }
}
