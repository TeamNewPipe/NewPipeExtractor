package org.schabi.newpipe.extractor.timeago;

import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class PatternsHolder {
    private final String wordSeparator;
    private final Collection<String> seconds;
    private final Collection<String> minutes;
    private final Collection<String> hours;
    private final Collection<String> days;
    private final Collection<String> weeks;
    private final Collection<String> months;
    private final Collection<String> years;

    private final Map<ChronoUnit, Map<String, Integer>> specialCases = new LinkedHashMap<>();

    protected PatternsHolder(String wordSeparator, Collection<String> seconds, Collection<String> minutes,
                             Collection<String> hours, Collection<String> days,
                             Collection<String> weeks, Collection<String> months, Collection<String> years) {
        this.wordSeparator = wordSeparator;
        this.seconds = seconds;
        this.minutes = minutes;
        this.hours = hours;
        this.days = days;
        this.weeks = weeks;
        this.months = months;
        this.years = years;
    }

    protected PatternsHolder(String wordSeparator, String[] seconds, String[] minutes, String[] hours, String[] days,
                             String[] weeks, String[] months, String[] years) {
        this(wordSeparator, List.of(seconds), List.of(minutes), List.of(hours), List.of(days),
                List.of(weeks), List.of(months), List.of(years));
    }

    public String wordSeparator() {
        return wordSeparator;
    }

    public Collection<String> seconds() {
        return seconds;
    }

    public Collection<String> minutes() {
        return minutes;
    }

    public Collection<String> hours() {
        return hours;
    }

    public Collection<String> days() {
        return days;
    }

    public Collection<String> weeks() {
        return weeks;
    }

    public Collection<String> months() {
        return months;
    }

    public Collection<String> years() {
        return years;
    }

    public Map<ChronoUnit, Map<String, Integer>> specialCases() {
        return specialCases;
    }

    protected void putSpecialCase(ChronoUnit unit, String caseText, int caseAmount) {
        Map<String, Integer> item = specialCases.computeIfAbsent(unit, k -> new LinkedHashMap<>());

        item.put(caseText, caseAmount);
    }

    public Map<ChronoUnit, Collection<String>> asMap() {
        return Map.of(
                ChronoUnit.SECONDS, seconds(),
                ChronoUnit.MINUTES, minutes(),
                ChronoUnit.HOURS, hours(),
                ChronoUnit.DAYS, days(),
                ChronoUnit.WEEKS, weeks(),
                ChronoUnit.MONTHS, months(),
                ChronoUnit.YEARS, years()
        );
    }
}
