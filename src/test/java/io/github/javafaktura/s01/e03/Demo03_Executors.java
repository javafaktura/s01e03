package io.github.javafaktura.s01.e03;

import io.github.javafaktura.s01.e03.domain.DailyRate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

@SuppressWarnings({"squid:S00101", "squid:S2925"})
class Demo03_Executors extends RatesTestSupport {

    // let's see what happens if we create a lot of threads
    @Test
    void createTooManyThreads() {
        for (long i = 0; i < 5_000; i++) {
            var thread = new Thread(this::longTask);
            thread.setDaemon(true);
            System.out.println("Starting " + thread.getName());
            thread.start();
        }
    }

    private void longTask() {
        try {
            Thread.sleep(Long.MAX_VALUE);       // long enough?
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }









    private ExecutorService executorService;

    @BeforeEach
    void initExecutorService() {
        executorService = Executors.newFixedThreadPool(50);

        // example of a custom executor service
//        executorService = customExecutorService(new AtomicInteger(0));
    }

    @Test
    void submitRunnables() throws InterruptedException {

        for (var day = FROM; TO.isAfter(day); day = day.plusDays(1)) {
            var date = day;

            Runnable task = () -> {
                var rate = client.getRate(date);
                compareWithLowest(rate);
            };

            executorService.submit(task);
        }

        System.out.println("All tasks submitted");

        executorService.shutdown();
        executorService.awaitTermination(10, SECONDS);
    }

    @Test
    void submitCallablesAndExtractResults() throws ExecutionException, InterruptedException {

        Collection<Future<DailyRate>> futures = new ArrayList<>();

        for (var day = FROM; TO.isAfter(day); day = day.plusDays(1)) {
            var date = day;

            Callable<DailyRate> task = () -> client.getRate(date);

            Future<DailyRate> result = executorService.submit(task);

            futures.add(result);
        }

        System.out.println("All tasks submitted");

        for (Future<DailyRate> future : futures) {
            var rate = future.get();
            compareWithLowest(rate);
        }
    }

    @Test
    @SkipRateSummary
    void scheduledExecution() throws ExecutionException, InterruptedException {
        ScheduledFuture<String> scheduledExecution = Executors.newScheduledThreadPool(1)
                .schedule(() -> "done", 1, SECONDS);
        System.out.println(scheduledExecution.get());   // see how long it took to execute this test
    }

    private ExecutorService customExecutorService(AtomicInteger idx) {
        /*
         * you might want to take a look at Guava's ThreadFactoryBuilder
         * or Apache commons BasicThreadFactory
         */
        return new ThreadPoolExecutor(
                1,
                8,
                500, MILLISECONDS,
                new LinkedBlockingDeque<>(),
                r -> {
                    Thread t = new Thread();
                    t.setName("Custom-" + idx.incrementAndGet());
                    return t;
                });
    }
}