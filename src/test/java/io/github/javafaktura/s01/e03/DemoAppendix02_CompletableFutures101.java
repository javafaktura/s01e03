package io.github.javafaktura.s01.e03;

import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * I strongly suggest reading the
 * <a href="https://www.nurkiewicz.com/2013/05/java-8-definitive-guide-to.html">
 * Tomasz Nurkiewicz's Definitive guide to CompletableFuture</a>.
 * It's a great starting point for learning how {@link CompletableFuture} works and
 * how you can chain and compose processing of the futures.
 */
@SuppressWarnings({"squid:S00101", "squid:S1481", "squid:S1854", "squid:S106", "unused"})
class DemoAppendix02_CompletableFutures101 {

    private Executor executor = Executors.newSingleThreadExecutor();

    @Test
    void create() {
        CompletableFuture<String> f1 = new CompletableFuture<>();
        CompletableFuture<String> f2 = supplyAsync(() -> "it took me while...");
        CompletableFuture<String> f3 = supplyAsync(() -> "it took me while...", executor);

        CompletableFuture<String> f4 = CompletableFuture.completedFuture("OK");
        CompletableFuture<String> f5 = CompletableFuture.failedFuture(new RuntimeException("KO"));

        CompletableFuture<Void> f6 = CompletableFuture.runAsync(() -> {
            // please do something useful
        });
    }

    @Test
    void getValue() throws ExecutionException, InterruptedException {

        assertEquals(
                "OK", complete().get(),
                "Unexpected result of CompletableFuture");

        assertEquals(
                "OK", complete().join(),
                "Unexpected result of CompletableFuture");

        assertEquals(
                "default",
                incomplete().getNow("default"), // change it to complete()
                "Future didn't return its internal result");

        /*
         * Call to join, get or getNow on failed future results in CompletionException
         * that wraps original exception thrown within the future.
         */
        assertThrows(
                CustomException.class,
                () -> {
                    try {
                        failed().join();
                    } catch (CompletionException e) {
                        throw e.getCause();
                    }
                },
                "Unexpected result of CompletableFuture");
    }

    @Test
    void completeFuture() {
        CompletableFuture<String> future = incomplete();
        future.complete("OK");                                    // future completed successfully
        future.completeExceptionally(new RuntimeException("KO"));       // ignored - future already complete
        assertFalse(
                future.isCompletedExceptionally(),
                "CompletableFuture exceptional completion cancelled its previous successful result");

        future.obtrudeException(new RuntimeException("USE WITH EXTREME CAUTION!")); // used to override result of the future
        assertTrue(
                future.isCompletedExceptionally(),
                "CompletableFuture exceptional completion didn't cancel its previous successful result");

        System.out.println(
                incomplete()
                        .orTimeout(1, SECONDS)
                        .exceptionally(Throwable::toString)
                        .join());
    }

    @Test
    void onCompletion() {
        complete().thenRun(new Runnable() {
            @Override
            public void run() {
                // this code will be executed after the future was completed
            }
        });

        complete().thenAccept(new Consumer<String>() {
            @Override
            public void accept(String x) {
                System.out.println(x);
            }
        });
    }

    @Test
    void thenApply() {
        CompletableFuture<Double> future = complete("STRING")
                .thenApply(String::toLowerCase)
                .thenApply(String::length)
                .thenApplyAsync(i -> i + 123)
                .thenApplyAsync(i -> Math.E * i, newFixedThreadPool(10));

        System.out.println(future.join());
    }

    @Test
    void thenCompose() {
        complete("text")
                .thenCompose(new Function<String, CompletionStage<String>>() {
                    @Override
                    public CompletionStage<String> apply(String text) {
                        return supplyAsync(() -> text.replace("t", ""));
                    }
                })
                .thenComposeAsync(new Function<String, CompletionStage<Integer>>() {
                    @Override
                    public CompletionStage<Integer> apply(String text) {
                        return supplyAsync(text::length);
                    }
                });
    }

    @Test
    void thenCombine() {
        var future = complete("foo")
                .thenCombine(complete("bar"), String::concat)
                .thenCombine(
                        complete(5),
                        (s, i) -> s.substring(0, i));
        System.out.println(future.join());
    }

    @Test
    void whenComplete() {
        // note that whenComplete doesn't change the type of the original future
        CompletableFuture<String> future = complete()
                .whenComplete((s, t) -> System.out.println(s));
    }

    @Test
    void exceptionHandling() {
        assertEquals(Integer.valueOf(-1),
                complete("test")
                        .thenApply(Integer::valueOf)
                        .exceptionally(e -> -1)
                        .join());

        assertEquals(Integer.valueOf(-1),
                complete("test")
                        .thenApply(Integer::valueOf)
                        .handle((i, ex) ->
                                ex == null ? i : Integer.valueOf(-1))
                        .join());
    }

    @Test
    void multipleFutures() {
        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(incomplete(), complete("result"));    // allOf will never complete
        System.out.println(anyOf.join());

        incomplete()
                .acceptEither(complete("result"), System.out::println)
                .join();

        CompletableFuture<Void> allOf = CompletableFuture.allOf(complete(), failed());
        assertThrows(CompletionException.class, allOf::join);

        complete("foo")
                .thenAcceptBoth(
                        complete("bar"),
                        (first, second) -> System.out.println("Both complete"))
                .join();
    }

    @Test
    void exceptionPropagation() {
        assertThrows(
                CompletionException.class,
                () ->
                        complete()
                                .thenApplyAsync(
                                        DemoAppendix02_CompletableFutures101::failingTransformation,
                                        newFixedThreadPool(1)) // dedicated thread in which the exception will be thrown
                                .thenApply(String::length)              // will pass through
                                .whenCompleteAsync((i, ex) -> {
                                    System.out.println("Final processing in thread " + Thread.currentThread().getName());
                                    if (ex != null) {
                                        System.out.println("Execution failed with " + ex);
                                    } else {
                                        System.out.println("Success! Result=" + i);
                                    }
                                }, newFixedThreadPool(1)) // dedicated thread for final processing
                                .join(),
                "Expected exception wasn't propagated to main thread");
    }











    private static <T> CompletableFuture<T> incomplete() {
        return new CompletableFuture<>();
    }

    private static <T> CompletableFuture<T> complete(T res) {
        return CompletableFuture.completedFuture(res);
    }

    private static CompletableFuture<String> complete() {
        return complete("OK");
    }

    private static <T> CompletableFuture<T> failed() {
        return CompletableFuture.failedFuture(new CustomException());
    }

    private static String failingTransformation(String s) {
        throw new CustomException();
    }

    private static class CustomException extends RuntimeException {
        CustomException() {
            super("Exception thrown in thread " + Thread.currentThread().getName());
        }
    }
}