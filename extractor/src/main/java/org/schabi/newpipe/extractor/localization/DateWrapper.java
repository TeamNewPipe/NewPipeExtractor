package org.schabi.newpipe.extractor.localization;

import org.schabi.newpipe.extractor.exceptions.ParsingException;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

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

    public DateWrapper(@Nonnull final LocalDateTime dateTime, final boolean isApproximation) {
        this(dateTime.atZone(ZoneId.systemDefault()).toInstant(), isApproximation);
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
     * @return the wrapped {@link Instant} as a {@link LocalDateTime} in the current time zone.
     */
    @Nonnull
    public LocalDateTime getLocalDateTime() {
        return getLocalDateTime(ZoneId.systemDefault());
    }

    /**
     * @return the wrapped {@link Instant} as a {@link LocalDateTime} in the given time zone.
     */
    @Nonnull
    public LocalDateTime getLocalDateTime(@Nonnull final ZoneId zoneId) {
        return LocalDateTime.ofInstant(instant, zoneId);
    }

    /**
     * @return if the date is considered is precise or just an approximation (e.g. service only
     * returns an approximation like 2 weeks ago instead of a precise date).
     */
    public boolean isApproximation() {
        return isApproximation;
    }

    @Override
    public String toString() {
        return "DateWrapper{"
                + "instant=" + instant
                + ", isApproximation=" + isApproximation
                + '}';
    }

    public static DateWrapper fromOffsetDateTime(final String date) throws ParsingException {
        try {
            return date != null ? new DateWrapper(OffsetDateTime.parse(date)) : null;
        } catch (final DateTimeParseException e) {
            throw new ParsingException("Could not parse date: \"" + date + "\"", e);
        }
    }

    public static DateWrapper fromInstant(final String date) throws ParsingException {
        try {
            return date != null ? new DateWrapper(Instant.parse(date)) : null;
        } catch (final DateTimeParseException e) {
            throw new ParsingException("Could not parse date: \"" + date + "\"", e);
        }
    }
}
