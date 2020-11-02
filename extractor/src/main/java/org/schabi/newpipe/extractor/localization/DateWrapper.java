package org.schabi.newpipe.extractor.localization;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * A wrapper class that provides a field to describe if the date/time is precise or just an approximation.
 */
public class DateWrapper implements Serializable {
    @NonNull private final OffsetDateTime offsetDateTime;
    private final boolean isApproximation;

    /**
     * @deprecated Use {@link #DateWrapper(OffsetDateTime)} instead.
     */
    @Deprecated
    public DateWrapper(@NonNull Calendar calendar) {
        this(calendar, false);
    }

    /**
     * @deprecated Use {@link #DateWrapper(OffsetDateTime, boolean)} instead.
     */
    @Deprecated
    public DateWrapper(@NonNull Calendar calendar, boolean isApproximation) {
        this(OffsetDateTime.ofInstant(calendar.toInstant(), ZoneOffset.UTC), isApproximation);
    }

    public DateWrapper(@NonNull OffsetDateTime offsetDateTime) {
        this(offsetDateTime, false);
    }

    public DateWrapper(@NonNull OffsetDateTime offsetDateTime, boolean isApproximation) {
        this.offsetDateTime = offsetDateTime.withOffsetSameInstant(ZoneOffset.UTC);
        this.isApproximation = isApproximation;
    }

    /**
     * @return the wrapped date/time as a {@link Calendar}.
     *
     * @deprecated use {@link #offsetDateTime()} instead.
     */
    @Deprecated
    @NonNull
    public Calendar date() {
        return GregorianCalendar.from(offsetDateTime.toZonedDateTime());
    }

    /**
     * @return the wrapped date/time.
     */
    @NonNull
    public OffsetDateTime offsetDateTime() {
        return offsetDateTime;
    }

    /**
     * @return if the date is considered is precise or just an approximation (e.g. service only returns an approximation
     * like 2 weeks ago instead of a precise date).
     */
    public boolean isApproximation() {
        return isApproximation;
    }
}
