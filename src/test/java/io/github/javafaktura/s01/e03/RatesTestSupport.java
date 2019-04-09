package io.github.javafaktura.s01.e03;

import io.github.javafaktura.s01.e03.domain.CurrencyExchangeClient;
import io.github.javafaktura.s01.e03.domain.DailyRate;
import io.github.javafaktura.s01.e03.mock.CurrencyExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

class RatesTestSupport {

    static final LocalDate TO = LocalDate.now();        // today
    static final LocalDate FROM = TO.minusYears(1);     // a year ago

    /** if true, we've got internets! */
    private static final boolean ONLINE = false;

    CurrencyExchangeClient client = new CurrencyExchangeClient(ONLINE ?
            "https://api.exchangeratesapi.io" :
            "http://localhost:4567");

    // used to monitor test execution time
    private Instant startedAt;

    private AtomicReference<DailyRate> lowest = new AtomicReference<>(null);
    private AtomicInteger numberOfComparisons = new AtomicInteger(0);

    /** Initiates {@link CurrencyExchange mock} if you're offline */
    @BeforeAll
    static void setupCurrencyExchangeService() {
        if (!ONLINE) {
            System.out.println("Starting CurrencyExchange mock");
            CurrencyExchange.start();
        }
    }

    /** resets internal state before each test */
    @BeforeEach
    void resetState() {
        startedAt = Instant.now();
        lowest.set(null);
        numberOfComparisons.set(0);
    }

    /** prints test summary (for tests without {@link SkipRateSummary} annotation) */
    @AfterEach
    void printResult(TestInfo testInfo) {
        testInfo.getTestMethod()
                .filter(m -> m.isAnnotationPresent(SkipRateSummary.class))
                .ifPresentOrElse(
                        annotation -> {/* skipping rate summary (NOP) */},
                        () -> System.out.println(String.format("%s performed %d comparisons to find the lowest rate=%s in %dms",
                                testInfo.getDisplayName(),
                                numberOfComparisons.get(),
                                lowest,
                                Duration.between(startedAt, Instant.now()).toMillis())));
    }

    /** Compares given rate with current lowest (and updates lowest if appropriate) */
    synchronized void compareWithLowest(DailyRate rate) {
        lowest.getAndUpdate(old ->
                DailyRate.min(old, rate)
                        .orElse(null));
        numberOfComparisons.incrementAndGet();
    }

    /**
     * Tests with this annotation won't print execution summary
     */
    @Target(METHOD)
    @Retention(RUNTIME)
    @interface SkipRateSummary {}
}