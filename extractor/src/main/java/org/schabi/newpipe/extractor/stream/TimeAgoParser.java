package org.schabi.newpipe.extractor.stream;

/*
 * Created by wojcik.online on 2018-01-25.
 */

import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * A helper class that is meant to be used by services that need to parse upload dates in the
 * format '2 days ago' or similar.
 */
public class TimeAgoParser {

    /**
     * A set of english phrases that are contained in the time units.
     * (e.g. '7 minutes ago' contains 'min')
     */
    public static Map<TimeAgoUnit, Collection<String>> DEFAULT_AGO_PHRASES =
            new EnumMap<>(TimeAgoUnit.class);

    private final Map<TimeAgoUnit, Collection<String>> agoPhrases;

    private final Calendar consistentNow;

    /**
     * Creates a helper to parse upload dates in the format '2 days ago'.
     * <p>
     *     Instantiate a new {@link TimeAgoParser} every time you extract a new batch of items.
     * </p>
     * @param agoPhrases A set of phrases how to recognize the time units in a given language.
     */
    public TimeAgoParser(Map<TimeAgoUnit, Collection<String>> agoPhrases) {
        this.agoPhrases = agoPhrases;
        consistentNow = Calendar.getInstance();
    }

    /**
     * Parses a textual date in the format '2 days ago' into a Calendar representation.
     * Beginning with days ago, marks the date as approximated by setting minutes, seconds
     * and milliseconds to 0.
     * @param textualDate The original date as provided by the streaming service
     * @return The parsed (approximated) time
     * @throws ParsingException if the time unit could not be recognized
     */
    public Calendar parse(String textualDate) throws ParsingException {
        int timeAgoAmount;
        try {
            timeAgoAmount = parseTimeAgoAmount(textualDate);
        } catch (NumberFormatException e) {
            // If there is no valid number in the textual date,
            // assume it is 1 (as in 'a second ago').
            timeAgoAmount = 1;
        }

        TimeAgoUnit timeAgoUnit = parseTimeAgoUnit(textualDate);
        return getCalendar(timeAgoAmount, timeAgoUnit);
    }

    private int parseTimeAgoAmount(String textualDate) throws NumberFormatException {
        String timeValueStr = textualDate.replaceAll("\\D+", "");
        return Integer.parseInt(timeValueStr);
    }

    private TimeAgoUnit parseTimeAgoUnit(String textualDate) throws ParsingException {
        for (TimeAgoUnit timeAgoUnit : agoPhrases.keySet()) {
            for (String agoPhrase : agoPhrases.get(timeAgoUnit)) {
                if (textualDate.toLowerCase().contains(agoPhrase.toLowerCase())){
                    return timeAgoUnit;
                }
            }
        }

        throw new ParsingException("Unable to parse the date: " + textualDate);
    }

    private Calendar getCalendar(int timeAgoAmount, TimeAgoUnit timeAgoUnit) {
        Calendar calendarTime = getNow();

        switch (timeAgoUnit) {
            case SECONDS:
                calendarTime.add(Calendar.SECOND, -timeAgoAmount);
                break;

            case MINUTES:
                calendarTime.add(Calendar.MINUTE, -timeAgoAmount);
                break;

            case HOURS:
                calendarTime.add(Calendar.HOUR_OF_DAY, -timeAgoAmount);
                break;

            case DAYS:
                calendarTime.add(Calendar.DAY_OF_MONTH, -timeAgoAmount);
                markApproximatedTime(calendarTime);
                break;

            case WEEKS:
                calendarTime.add(Calendar.WEEK_OF_YEAR, -timeAgoAmount);
                markApproximatedTime(calendarTime);
                break;

            case MONTHS:
                calendarTime.add(Calendar.MONTH, -timeAgoAmount);
                markApproximatedTime(calendarTime);
                break;

            case YEARS:
                calendarTime.add(Calendar.YEAR, -timeAgoAmount);
                // Prevent `PrettyTime` from showing '12 months ago'.
                calendarTime.add(Calendar.DAY_OF_MONTH, -1);
                markApproximatedTime(calendarTime);
                break;
        }

        return calendarTime;
    }

    private Calendar getNow() {
        return (Calendar) consistentNow.clone();
    }

    /**
     * Marks the time as approximated by setting minutes, seconds and milliseconds to 0.
     * @param calendarTime Time to be marked as approximated
     */
    private void markApproximatedTime(Calendar calendarTime) {
        calendarTime.set(Calendar.MINUTE, 0);
        calendarTime.set(Calendar.SECOND, 0);
        calendarTime.set(Calendar.MILLISECOND, 0);
    }

    static {
        DEFAULT_AGO_PHRASES.put(TimeAgoUnit.SECONDS, Collections.singleton("sec"));
        DEFAULT_AGO_PHRASES.put(TimeAgoUnit.MINUTES, Collections.singleton("min"));
        DEFAULT_AGO_PHRASES.put(TimeAgoUnit.HOURS, Collections.singleton("hour"));
        DEFAULT_AGO_PHRASES.put(TimeAgoUnit.DAYS, Collections.singleton("day"));
        DEFAULT_AGO_PHRASES.put(TimeAgoUnit.WEEKS, Collections.singleton("week"));
        DEFAULT_AGO_PHRASES.put(TimeAgoUnit.MONTHS, Collections.singleton("month"));
        DEFAULT_AGO_PHRASES.put(TimeAgoUnit.YEARS, Collections.singleton("year"));
    }

    public enum TimeAgoUnit {
        SECONDS,
        MINUTES,
        HOURS,
        DAYS,
        WEEKS,
        MONTHS,
        YEARS,
    }
}
