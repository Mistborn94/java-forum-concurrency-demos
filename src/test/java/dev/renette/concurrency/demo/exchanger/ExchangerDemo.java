package dev.renette.concurrency.demo.exchanger;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.renette.concurrency.demo.helper.Helper.generateCallablesList;
import static dev.renette.concurrency.demo.helper.Helper.log;

public class ExchangerDemo {

    @Test
    void exchangerDemo_2threads() throws InterruptedException {
        var exchanger = new Exchanger<DataBuffer>();
        int iterations = 3;
        int maxSize = 10;

        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.submit(new FillingLoop(exchanger, maxSize, iterations));
        executorService.submit(new EmptyingLoop(exchanger, iterations));

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.HOURS);
    }

    @Test
    @Disabled
        //This is incorrect usage.... The exchanger doesn't force a FillingLoop to exchange with an EmptyingLoop
        //In situations where we have multiple producers and consumers, exchangers are not the correct solution
    void exchangerDemo_multipleThreads() throws InterruptedException {
        var exchanger = new Exchanger<DataBuffer>();
        int iterations = 1;
        int maxSize = 10;

        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.invokeAll(generateCallablesList(4, () -> new FillingLoop(exchanger, maxSize, iterations)));
        executorService.invokeAll(generateCallablesList(4, () -> new EmptyingLoop(exchanger, iterations)));

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.HOURS);
    }

    static class DataBuffer {
        private static final AtomicInteger counter = new AtomicInteger(0);

        private final int id = counter.getAndIncrement();
        private int size = 0;

        void addItem() {
            size += 1;
        }

        void clearItem() {
            size -= 1;
        }

        int size() {
            return size;
        }

        @Override
        public String toString() {
            return "DataBuffer #" + id + " {size=" + size + "}";
        }
    }

    @AllArgsConstructor
    static class FillingLoop implements Callable<Boolean> {
        private final int sleepTime = ThreadLocalRandom.current().nextInt(100, 200);

        private final Exchanger<DataBuffer> exchanger;
        private final int maxSize;
        private final int iterations;

        @Override
        @SneakyThrows
        public Boolean call() {
            DataBuffer currentBuffer = new DataBuffer();

            for (int i = 0; i < iterations; i++) {
                logFilling();
                while (currentBuffer.size() < maxSize) {
                    Thread.sleep(sleepTime);
                    currentBuffer.addItem();
                }
                logFull();
                currentBuffer = exchanger.exchange(currentBuffer);
                logFullExchanged(currentBuffer);
            }
            return true;
        }
    }

    @AllArgsConstructor
    static class EmptyingLoop implements Callable<Boolean> {
        private final int sleepTime = ThreadLocalRandom.current().nextInt(100, 200);
        private final Exchanger<DataBuffer> exchanger;
        private final int iterations;

        @Override
        @SneakyThrows
        public Boolean call() {
            DataBuffer currentBuffer = new DataBuffer();

            for (int i = 0; i < iterations; i++) {
                logEmptying();
                while (currentBuffer.size() > 0) {
                    Thread.sleep(sleepTime);
                    currentBuffer.clearItem();
                }
                logEmpty();
                currentBuffer = exchanger.exchange(currentBuffer);
                logEmptyExchanged(currentBuffer);
            }
            return true;
        }
    }


    private static void logFullExchanged(DataBuffer currentBuffer) {
        log("\u001b[36;1mExchange complete, buffer is %s", currentBuffer);
    }

    private static void logFull() {
        log("\u001b[36;1mBuffer full, waiting for exchange");
    }

    private static void logFilling() {
        log("\u001b[36mFilling up the buffer");
    }

    private static void logEmptying() {
        log("\u001b[35mEmptying out the buffer");
    }

    private static void logEmpty() {
        log("\u001b[35;1mBuffer empty, waiting for exchange");
    }

    private static void logEmptyExchanged(DataBuffer currentBuffer) {
        log("\u001b[35;1mExchange complete, buffer is %s", currentBuffer);
    }

}
