package dev.renette.concurrency.demo.common;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Helper {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:m:s.SSS");

    public static void log(String message, Object... formatSpecifiers) {
        String formattedMessage = String.format(message, formatSpecifiers);
        System.out.printf("\u001b[0m%s %s [%s]%n", LocalTime.now().format(formatter), formattedMessage, Thread.currentThread().getName());
    }

    public static <T> List<Callable<T>> generateCallablesList(int count, Supplier<? extends Callable<T>> supplier) {
        return Stream.generate(supplier).limit(count).collect(Collectors.toList());
    }

}
