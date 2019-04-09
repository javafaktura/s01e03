package io.github.javafaktura.s01.e03;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings({"squid:S00101"})
class Demo01_Threads101 extends RatesTestSupport {

    /**
     * fugly "override" of {@link RatesTestSupport#FROM},
     * because sequential execution takes too long
     */
    private static final LocalDate FROM = TO.minusMonths(1);

    @Test
    void sequentialLoop() {

        for (var date = FROM; TO.isAfter(date); date = date.plusDays(1)) {
            var rate = client.getRate(date);
            compareWithLowest(rate);
        }
    }

    @Test
    void newThreads() throws InterruptedException {
//        Collection<Thread> threads = new ArrayList<>();

        for (var day = FROM; TO.isAfter(day); day = day.plusDays(1)) {
            // day cannot be used in anonymous types because it's not final
            final var date = day;

            Runnable runnable = () -> {
                var rate = client.getRate(date);
                compareWithLowest(rate);
            };

            Thread thread = new Thread(runnable);
//            threads.add(thread);
            thread.start();
        }

//        for (Thread thread : threads) {
//            thread.join();  // wait for all threads to finish execution
//        }
    }
}