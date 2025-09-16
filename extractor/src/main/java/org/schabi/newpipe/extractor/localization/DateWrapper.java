package org.schabi.newpipe.extractor.localization;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * A wrapper class that provides a field to describe if the date/time is precise or just an
 * approximation.
 */
public class DateWrapper implements Serializable {
    @Nonnull
    private final Instant instant;
    private final boolean isApproximation;

    public DateWrapper(@Nonnull final OffsetDateTime offsetDateTime) {
        this(offsetDateTime, false);
    }

    public DateWrapper(@Nonnull final OffsetDateTime offsetDateTime,
                       final boolean isApproximation) {
        this(offsetDateTime.toInstant(), isApproximation);
    }

    public DateWrapper(@Nonnull final Instant instant) {
        this(instant, false);
    }

    public DateWrapper(@Nonnull final Instant instant, final boolean isApproximation) {
        this.instant = instant;
        this.isApproximation = isApproximation;
    }

    public DateWrapper(@Nonnull final LocalDate localDate) {
        this(localDate, true);
    }

    public DateWrapper(@Nonnull final LocalDate localDate, final boolean isApproximation) {
        this(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), isApproximation);
    }

    /**
     * @return the wrapped {@link Instant}
     */
    @Nonnull
    public Instant getInstant() {
        return instant;
    }

    /**
     * @return the wrapped {@link Instant} as an {@link OffsetDateTime} set to UTC.
     */
    @Nonnull
    public OffsetDateTime offsetDateTime() {
        return instant.atOffset(ZoneOffset.UTC);
    }

    /**
     * @return the wrapped {@link Instant} as a {@link LocalDate} in the current time zone.
     */
    @Nonnull
    public LocalDate getLocalDate() {
        return LocalDate.ofInstant(instant, ZoneId.systemDefault());
    }

    /**
     * @return if the date is considered is precise or just an approximation (e.g. service only
     * returns an approximation like 2 weeks ago instead of a precise date).
     */
    public boolean isApproximation() {
        return isApproximation;
    }
}
