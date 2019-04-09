package io.github.javafaktura.s01.e03;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings({"squid:S00101"})
class Demo05_CompletableFutures extends RatesTestSupport {

    @Test
    void streamOfCompletableFutures() {

        /*
         * could be greatly simplified,
         * but this version shows a couple of interesting
         * CompletableFuture methods in action.
         */
        FROM.datesUntil(TO)
                .parallel()
                .map(CompletableFuture::completedFuture)
                .map(future -> future
                        .thenCompose(client::getRateAsync)
                        .thenAccept(this::compareWithLowest))
                .forEach(CompletableFuture::join);
    }
}