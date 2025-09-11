package org.schabi.newpipe.extractor.localization;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.time.Instant;
import java.time.OffsetDateTime;
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
     * @return if the date is considered is precise or just an approximation (e.g. service only
     * returns an approximation like 2 weeks ago instead of a precise date).
     */
    public boolean isApproximation() {
        return isApproximation;
    }
}
