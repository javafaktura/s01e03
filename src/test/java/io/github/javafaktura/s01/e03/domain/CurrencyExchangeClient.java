package io.github.javafaktura.s01.e03.domain;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

/**
 * Wraps HTTP client that calls external currency exchange service
 * and extracts PLN rate for provided date.
 *
 * Provides sync (blocking - {@link CurrencyExchangeClient#getRate(LocalDate)})
 * and async (based on {@link CompletableFuture} - {@link CurrencyExchangeClient#getRateAsync(LocalDate)})
 * implementations.
 */
public class CurrencyExchangeClient {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private final Function<LocalDate, URI> uri;

    public CurrencyExchangeClient(String urlBase) {
        this.uri = date -> URI.create(urlBase + "/" + date.format(ISO_LOCAL_DATE) + "?symbols=PLN");
    }

    /** synchronous variant */
    public DailyRate getRate(LocalDate date) {
        var res = get(uri.apply(date));
        var plnVal = extractPln(res);
        var rate = new BigDecimal(plnVal);
        var dailyRate = new DailyRate(date, rate);
        print(dailyRate);
        return dailyRate;
    }

    /** asynchronous variant */
    public CompletableFuture<DailyRate> getRateAsync(LocalDate date) {
        return getAsync(uri.apply(date))
                .thenApply(this::extractPln)
                .thenApply(BigDecimal::new)
                .thenApply(rate -> new DailyRate(date, rate))
                .whenComplete((rate, ex) -> print(rate));
    }

    private String extractPln(String json) {
        return json
                .replaceFirst("^.*\"PLN\":", "")
                .replaceFirst("[},].*$", "");
    }

    private String get(URI uri) {
        var request = HttpRequest.newBuilder(uri).GET().build();
        try {
            return HTTP_CLIENT
                    .send(request, HttpResponse.BodyHandlers.ofString())
                    .body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("HTTP client threw exception when trying to execute GET on " + uri, e);
        }
    }

    private CompletableFuture<String> getAsync(URI uri) {
        var request = HttpRequest.newBuilder(uri).GET().build();

        return HTTP_CLIENT
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    private void print(DailyRate rate) {
        System.out.println(
                String.format("[%s]\t%s", Thread.currentThread().getName(), rate));
    }
}