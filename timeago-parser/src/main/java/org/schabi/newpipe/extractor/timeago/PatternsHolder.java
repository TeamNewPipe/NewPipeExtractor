package org.schabi.newpipe.extractor.timeago;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Arrays.asList;

public abstract class PatternsHolder {
    private final String wordSeparator;
    private final Collection<String> seconds;
    private final Collection<String> minutes;
    private final Collection<String> hours;
    private final Collection<String> days;
    private final Collection<String> weeks;
    private final Collection<String> months;
    private final Collection<String> years;

    private final Map<TimeAgoUnit, Map<String, Integer>> specialCases = new LinkedHashMap<>();

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
        this(wordSeparator, asList(seconds), asList(minutes), asList(hours), asList(days),
                asList(weeks), asList(months), asList(years));
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

    public Map<TimeAgoUnit, Map<String, Integer>> specialCases() {
        return specialCases;
    }

    protected void putSpecialCase(TimeAgoUnit unit, String caseText, int caseAmount) {
        Map<String, Integer> item = specialCases.get(unit);

        if (item == null) {
            item = new LinkedHashMap<>();
            specialCases.put(unit, item);
        }

        item.put(caseText, caseAmount);
    }

    public Map<TimeAgoUnit, Collection<String>> asMap() {
        final Map<TimeAgoUnit, Collection<String>> returnMap = new LinkedHashMap<>();
        returnMap.put(TimeAgoUnit.SECONDS, seconds());
        returnMap.put(TimeAgoUnit.MINUTES, minutes());
        returnMap.put(TimeAgoUnit.HOURS, hours());
        returnMap.put(TimeAgoUnit.DAYS, days());
        returnMap.put(TimeAgoUnit.WEEKS, weeks());
        returnMap.put(TimeAgoUnit.MONTHS, months());
        returnMap.put(TimeAgoUnit.YEARS, years());

        return returnMap;
    }
}
