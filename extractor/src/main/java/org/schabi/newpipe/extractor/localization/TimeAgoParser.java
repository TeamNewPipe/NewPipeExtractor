package org.schabi.newpipe.extractor.localization;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.timeago.PatternsHolder;
import org.schabi.newpipe.extractor.timeago.TimeAgoUnit;
import org.schabi.newpipe.extractor.utils.Parser;

import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A helper class that is meant to be used by services that need to parse upload dates in the
 * format '2 days ago' or similar.
 */
public class TimeAgoParser {
    private final PatternsHolder patternsHolder;
    private final Calendar consistentNow;

    /**
     * Creates a helper to parse upload dates in the format '2 days ago'.
     * <p>
     * Instantiate a new {@link TimeAgoParser} every time you extract a new batch of items.
     * </p>
     *
     * @param patternsHolder An object that holds the "time ago" patterns, special cases, and the language word separator.
     */
    public TimeAgoParser(PatternsHolder patternsHolder) {
        this.patternsHolder = patternsHolder;
        consistentNow = Calendar.getInstance();
    }

    /**
     * Parses a textual date in the format '2 days ago' into a Calendar representation which is then wrapped in a
     * {@link DateWrapper} object.
     * <p>
     * Beginning with days ago, the date is considered as an approximation.
     *
     * @param textualDate The original date as provided by the streaming service
     * @return The parsed time (can be approximated)
     * @throws ParsingException if the time unit could not be recognized
     */
    public DateWrapper parse(String textualDate) throws ParsingException {
        for (Map.Entry<TimeAgoUnit, Map<String, Integer>> caseUnitEntry : patternsHolder.specialCases().entrySet()) {
            final TimeAgoUnit timeAgoUnit = caseUnitEntry.getKey();
            for (Map.Entry<String, Integer> caseMapToAmountEntry : caseUnitEntry.getValue().entrySet()) {
                final String caseText = caseMapToAmountEntry.getKey();
                final Integer caseAmount = caseMapToAmountEntry.getValue();

                if (textualDateMatches(textualDate, caseText)) {
                    return getResultFor(caseAmount, timeAgoUnit);
                }
            }
        }

        int timeAgoAmount;
        try {
            timeAgoAmount = parseTimeAgoAmount(textualDate);
        } catch (NumberFormatException e) {
            // If there is no valid number in the textual date,
            // assume it is 1 (as in 'a second ago').
            timeAgoAmount = 1;
        }

        final TimeAgoUnit timeAgoUnit = parseTimeAgoUnit(textualDate);
        return getResultFor(timeAgoAmount, timeAgoUnit);
    }

    private int parseTimeAgoAmount(String textualDate) throws NumberFormatException {
        String timeValueStr = textualDate.replaceAll("\\D+", "");
        return Integer.parseInt(timeValueStr);
    }

    private TimeAgoUnit parseTimeAgoUnit(String textualDate) throws ParsingException {
        for (Map.Entry<TimeAgoUnit, Collection<String>> entry : patternsHolder.asMap().entrySet()) {
            final TimeAgoUnit timeAgoUnit = entry.getKey();

            for (String agoPhrase : entry.getValue()) {
                if (textualDateMatches(textualDate, agoPhrase)) {
                    return timeAgoUnit;
                }
            }
        }

        throw new ParsingException("Unable to parse the date: " + textualDate);
    }

    private boolean textualDateMatches(String textualDate, String agoPhrase) {
        if (textualDate.equals(agoPhrase)) {
            return true;
        }

        if (patternsHolder.wordSeparator().isEmpty()) {
            return textualDate.toLowerCase().contains(agoPhrase.toLowerCase());
        } else {
            final String escapedPhrase = Pattern.quote(agoPhrase.toLowerCase());
            final String escapedSeparator;
            if (patternsHolder.wordSeparator().equals(" ")) {
                // From JDK8 → \h - Treat horizontal spaces as a normal one (non-breaking space, thin space, etc.)
                escapedSeparator = "[ \\t\\xA0\\u1680\\u180e\\u2000-\\u200a\\u202f\\u205f\\u3000]";
            } else {
                escapedSeparator = Pattern.quote(patternsHolder.wordSeparator());
            }

            // (^|separator)pattern($|separator)
            // Check if the pattern is surrounded by separators or start/end of the string.
            final String pattern =
                    "(^|" + escapedSeparator + ")" + escapedPhrase + "($|" + escapedSeparator + ")";

            return Parser.isMatch(pattern, textualDate.toLowerCase());
        }
    }

    private DateWrapper getResultFor(int timeAgoAmount, TimeAgoUnit timeAgoUnit) {
        final Calendar calendarTime = getNow();
        boolean isApproximation = false;

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
                isApproximation = true;
                break;

            case WEEKS:
                calendarTime.add(Calendar.WEEK_OF_YEAR, -timeAgoAmount);
                isApproximation = true;
                break;

            case MONTHS:
                calendarTime.add(Calendar.MONTH, -timeAgoAmount);
                isApproximation = true;
                break;

            case YEARS:
                calendarTime.add(Calendar.YEAR, -timeAgoAmount);
                // Prevent `PrettyTime` from showing '12 months ago'.
                calendarTime.add(Calendar.DAY_OF_MONTH, -1);
                isApproximation = true;
                break;
        }

        if (isApproximation) {
            markApproximatedTime(calendarTime);
        }

        return new DateWrapper(calendarTime, isApproximation);
    }

    private Calendar getNow() {
        return (Calendar) consistentNow.clone();
    }

    /**
     * Marks the time as approximated by setting minutes, seconds and milliseconds to 0.
     *
     * @param calendarTime Time to be marked as approximated
     */
    private void markApproximatedTime(Calendar calendarTime) {
        calendarTime.set(Calendar.MINUTE, 0);
        calendarTime.set(Calendar.SECOND, 0);
        calendarTime.set(Calendar.MILLISECOND, 0);
    }
}
