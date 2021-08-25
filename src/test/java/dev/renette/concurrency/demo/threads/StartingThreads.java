package dev.renette.concurrency.demo.threads;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static dev.renette.concurrency.demo.helper.Helper.log;

class StartingThreads {

    @Test
    void threadWithRunnable() throws InterruptedException {
        log("Starting");
        var thread = new Thread(() -> log("Running in thread"));
        thread.start();
        log("Finished");
        thread.join();
    }

    @Test
    void parallelStream() {
        var numbersList = initList(10);
        log("Starting");
        var sum = numbersList.parallelStream()
                .peek(i -> log("Seeing value [%d]", i))
                .map(i -> i * i)
                .reduce(Integer::sum)
                .orElse(0);

        log("Finished. Result is %d", sum);
    }


    @Test
    void executorService() throws InterruptedException, ExecutionException {
        var executorService = Executors.newFixedThreadPool(5);

        //Runnable
        var runnableFuture = executorService.submit(() -> log("Runnable"));
        //Callable
        var callableFuture = executorService.submit(() -> {
            log("Callabe");
            return "Running";
        });

        //Multiple callables
        var futuresList = executorService.invokeAll(
                initCallableList(20)
        );

        runnableFuture.get();
        callableFuture.get();
        futuresList.forEach(future -> {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    void completableFuture() throws InterruptedException, ExecutionException {
        //Supplier
        var future = CompletableFuture.supplyAsync(() -> {
            log("Supplier");
            return "Value";
        });
        //Runnable
        CompletableFuture.runAsync(() -> log("Runnable"));
    }


    private List<Callable<String>> initCallableList(int size) {
        return initList(size).stream().map(this::makeCallable).collect(Collectors.toList());
    }

    private Callable<String> makeCallable(Integer i) {
        return () -> {
            log("Item " + i);
            return "Item " + i;
        };
    }

    private List<Integer> initList(int size) {
        ArrayList<Integer> integers = new ArrayList<>(size);

        for (int i = 0; i < size; ++i) {
            integers.add(i);
        }
        return integers;
    }

}
