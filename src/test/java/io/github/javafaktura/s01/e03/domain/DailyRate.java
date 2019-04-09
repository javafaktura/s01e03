package io.github.javafaktura.s01.e03.domain;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;

public class DailyRate implements Comparable<DailyRate> {

    /**
     * @return min of a and b based on {@link DailyRate#compareTo(DailyRate)}
     *         (or empty if both were null)
     */
    public static Optional<DailyRate> min(DailyRate a, DailyRate b) {
        return Stream.of(a, b)
                .filter(Objects::nonNull)
                .min(DailyRate::compareTo);
    }

    private final BigDecimal rate;
    private final LocalDate date;

    DailyRate(LocalDate date, BigDecimal rate) {
        this.rate = requireNonNull(rate, "rate cannot be null");
        this.date = requireNonNull(date, "date cannot be null");
    }

    private BigDecimal getRate() {
        return rate;
    }

    private LocalDate getDate() {
        return date;
    }

    @Override
    public String toString() {
        return new DecimalFormat("#.0000").format(rate) + " (@ " + date + ")";
    }

    /** compares rate, then date */
    @Override
    public int compareTo(DailyRate that) {
        return comparing(DailyRate::getRate)
                .thenComparing(DailyRate::getDate)
                .compare(this, that);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DailyRate)) return false;
        DailyRate dailyRate = (DailyRate) o;
        return Objects.equals(rate, dailyRate.rate) &&
                Objects.equals(date, dailyRate.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, rate);
    }
}