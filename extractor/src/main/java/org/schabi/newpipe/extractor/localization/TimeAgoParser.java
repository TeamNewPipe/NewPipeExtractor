package org.schabi.newpipe.extractor.localization;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.timeago.PatternsHolder;
import org.schabi.newpipe.extractor.utils.Parser;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.MatchResult;

/**
 * A helper class that is meant to be used by services that need to parse durations such as
 * {@code 23 seconds} and/or upload dates in the format {@code 2 days ago} or similar.
 */
public class TimeAgoParser {

    private static final Pattern DURATION_PATTERN = Pattern.compile("(?:(\\d+) )?([A-z]+)");

    private final PatternsHolder patternsHolder;
    private final OffsetDateTime now;

    /**
     * Creates a helper to parse upload dates in the format '2 days ago'.
     * <p>
     * Instantiate a new {@link TimeAgoParser} every time you extract a new batch of items.
     * </p>
     *
     * @param patternsHolder An object that holds the "time ago" patterns, special cases, and the
     *                       language word separator.
     */
    public TimeAgoParser(final PatternsHolder patternsHolder) {
        this.patternsHolder = patternsHolder;
        now = OffsetDateTime.now(ZoneOffset.UTC);
    }

    /**
     * Parses a textual date in the format '2 days ago' into a Calendar representation which is then
     * wrapped in a {@link DateWrapper} object.
     * <p>
     * Beginning with days ago, the date is considered as an approximation.
     *
     * @param textualDate The original date as provided by the streaming service
     * @return The parsed time (can be approximated)
     * @throws ParsingException if the time unit could not be recognized
     */
    public DateWrapper parse(final String textualDate) throws ParsingException {
        for (final Map.Entry<ChronoUnit, Map<String, Integer>> caseUnitEntry
                : patternsHolder.specialCases().entrySet()) {
            final ChronoUnit chronoUnit = caseUnitEntry.getKey();
            for (final Map.Entry<String, Integer> caseMapToAmountEntry
                    : caseUnitEntry.getValue().entrySet()) {
                final String caseText = caseMapToAmountEntry.getKey();
                final Integer caseAmount = caseMapToAmountEntry.getValue();

                if (textualDateMatches(textualDate, caseText)) {
                    return getResultFor(caseAmount, chronoUnit);
                }
            }
        }

        return getResultFor(parseTimeAgoAmount(textualDate), parseChronoUnit(textualDate));
    }

    /**
     * Parses a textual duration into a duration computer number.
     *
     * @param textualDuration the textual duration to parse
     * @return the textual duration parsed, as a primitive {@code long}
     * @throws ParsingException if the textual duration could not be parsed
     */
    public long parseDuration(final String textualDuration) throws ParsingException {
        // We can't use Matcher.results, as it is only available on Android 14 and above
        final Matcher matcher = DURATION_PATTERN.matcher(textualDuration);
        final List<MatchResult> results = new ArrayList<>();
        while (matcher.find()) {
            results.add(matcher.toMatchResult());
        }

        return results.stream()
                .map(match -> {
                    final String digits = match.group(1);
                    final String word = match.group(2);

                    int amount;
                    try {
                        amount = Integer.parseInt(digits);
                    } catch (final NumberFormatException ignored) {
                        amount = 1;
                    }

                    final ChronoUnit unit;
                    try {
                        unit = parseChronoUnit(word);
                    } catch (final ParsingException ignored) {
                        return 0L;
                    }

                    return amount * unit.getDuration().getSeconds();
                })
                .filter(n -> n > 0)
                .reduce(Long::sum)
                .orElseThrow(() -> new ParsingException(
                        "Could not parse duration \"" + textualDuration + "\""));
    }

    private int parseTimeAgoAmount(final String textualDate) {
        try {
            return Integer.parseInt(textualDate.replaceAll("\\D+", ""));
        } catch (final NumberFormatException ignored) {
            // If there is no valid number in the textual date,
            // assume it is 1 (as in 'a second ago').
            return 1;
        }
    }

    private ChronoUnit parseChronoUnit(final String textualDate) throws ParsingException {
        return patternsHolder.asMap().entrySet().stream()
                .filter(e -> e.getValue().stream()
                        .anyMatch(agoPhrase -> textualDateMatches(textualDate, agoPhrase)))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() ->
                        new ParsingException("Unable to parse the date: " + textualDate));
    }

    private boolean textualDateMatches(final String textualDate, final String agoPhrase) {
        if (textualDate.equals(agoPhrase)) {
            return true;
        }

        if (patternsHolder.wordSeparator().isEmpty()) {
            return textualDate.toLowerCase().contains(agoPhrase.toLowerCase());
        }

        final String escapedPhrase = Pattern.quote(agoPhrase.toLowerCase());
        final String escapedSeparator = patternsHolder.wordSeparator().equals(" ")
                // From JDK8 → \h - Treat horizontal spaces as a normal one
                // (non-breaking space, thin space, etc.)
                // Also split the string on numbers to be able to parse strings like "2wk"
                ? "[ \\t\\xA0\\u1680\\u180e\\u2000-\\u200a\\u202f\\u205f\\u3000\\d]"
                : Pattern.quote(patternsHolder.wordSeparator());

        // (^|separator)pattern($|separator)
        // Check if the pattern is surrounded by separators or start/end of the string.
        final String pattern =
                "(^|" + escapedSeparator + ")" + escapedPhrase + "($|" + escapedSeparator + ")";

        return Parser.isMatch(pattern, textualDate.toLowerCase());
    }

    private DateWrapper getResultFor(final int timeAgoAmount, final ChronoUnit chronoUnit) {
        OffsetDateTime offsetDateTime = now;
        boolean isApproximation = false;

        switch (chronoUnit) {
            case SECONDS:
            case MINUTES:
            case HOURS:
                offsetDateTime = offsetDateTime.minus(timeAgoAmount, chronoUnit);
                break;

            case DAYS:
            case WEEKS:
            case MONTHS:
                offsetDateTime = offsetDateTime.minus(timeAgoAmount, chronoUnit);
                isApproximation = true;
                break;

            case YEARS:
                // minusDays is needed to prevent `PrettyTime` from showing '12 months ago'.
                offsetDateTime = offsetDateTime.minusYears(timeAgoAmount).minusDays(1);
                isApproximation = true;
                break;
        }

        if (isApproximation) {
            offsetDateTime = offsetDateTime.truncatedTo(ChronoUnit.HOURS);
        }

        return new DateWrapper(offsetDateTime, isApproximation);
    }
}
