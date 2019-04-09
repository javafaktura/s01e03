package io.github.javafaktura.s01.e03;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

@SuppressWarnings({"squid:S00101"})
class Demo04_ParallelStreams extends RatesTestSupport {

    @Test
    void parallelStream() {
        FROM.datesUntil(TO)
                .parallel()
                .map(client::getRate)
                .forEach(this::compareWithLowest);
    }

    @Test
    void parallelStreamWithCustomParallelism() throws ExecutionException, InterruptedException {
        new ForkJoinPool(40)
                .submit(this::parallelStream)
                .get();
    }
}