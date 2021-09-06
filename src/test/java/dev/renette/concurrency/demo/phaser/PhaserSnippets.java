package dev.renette.concurrency.demo.phaser;

import dev.renette.concurrency.demo.common.ConcurrencyDemo;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.Phaser;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.renette.concurrency.demo.common.Helper.generateCallablesList;

public class PhaserSnippets extends ConcurrencyDemo {

    @Test
    void phaserDemo() throws InterruptedException {
        int tasks = 30;

        Phaser phaser = new Phaser();

        executorService.submit(new WaitingTask(phaser));
        executorService.invokeAll(generateCallablesList(tasks, () -> new ArrivingTask(phaser)));
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
