package dev.renette.concurrency.demo.phaser;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.renette.concurrency.demo.helper.Helper.generateCallablesList;

public class PhaserSnippets {

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
                phaser.awaitAdvance(phase);
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

            while (true) {
                if (shouldContinue()) {
                    phaser.arriveAndAwaitAdvance();
                } else {
                    phaser.arriveAndDeregister();
                    break;
                }
            }


            return name;
        }

        private boolean shouldContinue() {
            return false;
        }
    }
}
