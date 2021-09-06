package dev.renette.concurrency.demo.threads;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static dev.renette.concurrency.demo.common.Helper.log;

class StartingThreads {

    @Test
    void threadWithRunnable() throws InterruptedException {
        log("Starting");
        var thread = new Thread(() -> log("Running in thread"));
        thread.start();
        log("In Progress");
        thread.join();
        log("Finished");
    }

    @Test
    void parallelStream() {
        log("Starting");
        var sum = IntStream.range(0, 10)
                .parallel()
                .peek(i -> log("Seeing value [%d]", i))
                .map(i -> i * i)
                .reduce(Integer::sum)
                .orElse(0);

        log("Finished. Result is %d", sum);
    }


    @Test
    void executorService() throws InterruptedException, ExecutionException {
        var executorService = Executors.newCachedThreadPool();

        //Runnable
        var runnableFuture = executorService.submit(() -> log("Runnable"));
        runnableFuture.get();
        //Callable
        var callableFuture = executorService.submit(() -> {
            log("Callable");
            return true;
        });
        callableFuture.get();

        //Multiple callables
        var futuresList = executorService.invokeAll(initCallableList(20));

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
    void completableFuture() throws ExecutionException, InterruptedException {
        //Supplier
        var stringFuture = CompletableFuture.supplyAsync(() -> {
            log("Supplier");
            return "Value";
        });
        //Runnable
        var voidFuture = CompletableFuture.runAsync(() -> log("Runnable"));

        String stringValue = stringFuture.get();
        voidFuture.get();
    }

    private List<Callable<Boolean>> initCallableList(int size) {
        return IntStream.range(0, size).mapToObj(this::makeCallable).collect(Collectors.toList());
    }

    private Callable<Boolean> makeCallable(Integer i) {
        return () -> {
            log("Inside callable " + i);
            return true;
        };
    }

}
