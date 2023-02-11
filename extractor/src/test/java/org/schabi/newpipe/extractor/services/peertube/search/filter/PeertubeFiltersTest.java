// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.peertube.search.filter;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.search.filter.BaseSearchFilters;
import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.extractor.services.DefaultFiltersTest;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeertubeFiltersTest extends DefaultFiltersTest {

    ResultFixer fixResults;

    PeertubeFiltersTest() {

    }

    @Override
    protected BaseSearchFilters setupPriorTesting() {
        fixResults = new ResultFixer();
        fixResults.searchAndReplaceMap.put("startDate=([0-9:T\\-\\.])*", "startDate=X");
        return new PeertubeFilters();
    }

    @Override
    protected String emptyContentFilterTestSetup(final BaseSearchFilters base) {
        return "";
    }

    @Override
    protected void validContentFilterWithAllSortFiltersTestSetup(
            final List<InputAndExpectedResultData> validContentFiltersWithExpectedResult) {

        validContentFiltersWithExpectedResult.add(new InputAndExpectedResultData(
                null,
                singletonList(PeertubeFilters.ID_CF_MAIN_VIDEOS),
                getAllSortFiltersIdsList(),
                "&sort=match&sort=name&sort=duration&sort=publishedAt&sort=createdAt&sort=views&sort=likes&isLive=true&isLive=false&nsfw=true&nsfw=false&startDate=X&startDate=X&startDate=X&startDate=X&durationMax=240&durationMin=240&durationMax=600&durationMin=600&resultType=videos",
                null,
                null,
                fixResults
        ));
        validContentFiltersWithExpectedResult.add(new InputAndExpectedResultData(
                null,
                singletonList(PeertubeFilters.ID_CF_MAIN_CHANNELS),
                getAllSortFiltersIdsList(),
                "&sort=match&sort=name&sort=duration&sort=publishedAt&sort=createdAt&sort=views&sort=likes&isLive=true&isLive=false&nsfw=true&nsfw=false&startDate=X&startDate=X&startDate=X&startDate=X&durationMax=240&durationMin=240&durationMax=600&durationMin=600&resultType=channels",
                null,
                null,
                fixResults
        ));
        validContentFiltersWithExpectedResult.add(new InputAndExpectedResultData(
                null,
                singletonList(PeertubeFilters.ID_CF_MAIN_PLAYLISTS),
                getAllSortFiltersIdsList(),
                "&sort=match&sort=name&sort=duration&sort=publishedAt&sort=createdAt&sort=views&sort=likes&isLive=true&isLive=false&nsfw=true&nsfw=false&startDate=X&startDate=X&startDate=X&startDate=X&durationMax=240&durationMin=240&durationMax=600&durationMin=600&resultType=playlists",
                null,
                null,
                fixResults
        ));
    }

    @Override
    protected void validContentFilterSetup(
            final List<InputAndExpectedResultData> validContentFiltersAndExpectedResults) {
        validContentFiltersAndExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(PeertubeFilters.ID_CF_MAIN_VIDEOS),
                null,
                "&resultType=videos",
                null,
                null,
                null
        ));
        validContentFiltersAndExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(PeertubeFilters.ID_CF_MAIN_CHANNELS),
                null,
                "&resultType=channels",
                null,
                null,
                null
        ));
        validContentFiltersAndExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(PeertubeFilters.ID_CF_MAIN_PLAYLISTS),
                null,
                "&resultType=playlists",
                null,
                null,
                null
        ));
    }

    @Override
    protected void validAllSortFilterSetup(
            final List<InputAndExpectedResultData> validAllSortFilters) {

        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(PeertubeFilters.ID_SF_SORT_BY_RELEVANCE),
                "&sort=-match",
                null,
                null,
                fixResults
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(PeertubeFilters.ID_SF_SORT_BY_NAME),
                "&sort=-name",
                null,
                null,
                fixResults
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(PeertubeFilters.ID_SF_SORT_BY_DURATION),
                "&sort=-duration",
                null,
                null,
                fixResults
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(PeertubeFilters.ID_SF_SORT_BY_PUBLISH_DATE),
                "&sort=-publishedAt",
                null,
                null,
                fixResults
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(PeertubeFilters.ID_SF_SORT_BY_CREATION_DATE),
                "&sort=-createdAt",
                null,
                null,
                fixResults
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(PeertubeFilters.ID_SF_SORT_BY_VIEWS),
                "&sort=-views",
                null,
                null,
                fixResults
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(PeertubeFilters.ID_SF_SORT_BY_LIKES),
                "&sort=-likes",
                null,
                null,
                fixResults
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(PeertubeFilters.ID_SF_KIND_ALL),
                "",
                null,
                null,
                fixResults
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(PeertubeFilters.ID_SF_KIND_LIVE),
                "&isLive=true",
                null,
                null,
                fixResults
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(PeertubeFilters.ID_SF_KIND_VOD_VIDEOS),
                "&isLive=false",
                null,
                null,
                fixResults
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(PeertubeFilters.ID_SF_SENSITIVE_ALL),
                "",
                null,
                null,
                fixResults
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(PeertubeFilters.ID_SF_SENSITIVE_YES),
                "&nsfw=true",
                null,
                null,
                fixResults
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(PeertubeFilters.ID_SF_SENSITIVE_NO),
                "&nsfw=false",
                null,
                null,
                fixResults
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(PeertubeFilters.ID_SF_PUBLISHED_ALL),
                "",
                null,
                null,
                fixResults
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(PeertubeFilters.ID_SF_PUBLISHED_TODAY),
                "&startDate=X",
                null,
                null,
                fixResults
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(PeertubeFilters.ID_SF_PUBLISHED_LAST_7_DAYS),
                "&startDate=X",
                null,
                null,
                fixResults
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(PeertubeFilters.ID_SF_PUBLISHED_LAST_30_DAYS),
                "&startDate=X",
                null,
                null,
                fixResults
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(PeertubeFilters.ID_SF_PUBLISHED_LAST_YEAR),
                "&startDate=X",
                null,
                null,
                fixResults
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(PeertubeFilters.ID_SF_DURATION_ALL),
                "",
                null,
                null,
                fixResults
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(PeertubeFilters.ID_SF_DURATION_SHORT),
                "&durationMax=240",
                null,
                null,
                fixResults
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(PeertubeFilters.ID_SF_DURATION_MEDIUM),
                "&durationMin=240&durationMax=600",
                null,
                null,
                fixResults
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(PeertubeFilters.ID_SF_DURATION_LONG),
                "&durationMin=600",
                null,
                null,
                fixResults
        ));

    }

    @Override
    protected void validContentFilterAllSortFiltersTestSetup(
            final List<InputAndExpectedResultData>
                    validContentFilterAllSortFiltersExpectedResults) {
        // We do not include ID_SF_SORT_ORDER_ASCENDING as we test this special in another testcase
        // --> validSortFiltersWithAscendingTest()
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(PeertubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(PeertubeFilters.ID_SF_SORT_BY_RELEVANCE),
                "&sort=-match&resultType=videos",
                null,
                null,
                fixResults
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(PeertubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(PeertubeFilters.ID_SF_SORT_BY_NAME),
                "&sort=-name&resultType=videos",
                null,
                null,
                fixResults
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(PeertubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(PeertubeFilters.ID_SF_SORT_BY_DURATION),
                "&sort=-duration&resultType=videos",
                null,
                null,
                fixResults
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(PeertubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(PeertubeFilters.ID_SF_SORT_BY_PUBLISH_DATE),
                "&sort=-publishedAt&resultType=videos",
                null,
                null,
                fixResults
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(PeertubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(PeertubeFilters.ID_SF_SORT_BY_CREATION_DATE),
                "&sort=-createdAt&resultType=videos",
                null,
                null,
                fixResults
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(PeertubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(PeertubeFilters.ID_SF_SORT_BY_VIEWS),
                "&sort=-views&resultType=videos",
                null,
                null,
                fixResults
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(PeertubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(PeertubeFilters.ID_SF_SORT_BY_LIKES),
                "&sort=-likes&resultType=videos",
                null,
                null,
                fixResults
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(PeertubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(PeertubeFilters.ID_SF_KIND_ALL),
                "&resultType=videos",
                null,
                null,
                fixResults
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(PeertubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(PeertubeFilters.ID_SF_KIND_LIVE),
                "&isLive=true&resultType=videos",
                null,
                null,
                fixResults
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(PeertubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(PeertubeFilters.ID_SF_KIND_VOD_VIDEOS),
                "&isLive=false&resultType=videos",
                null,
                null,
                fixResults
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(PeertubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(PeertubeFilters.ID_SF_SENSITIVE_ALL),
                "&resultType=videos",
                null,
                null,
                fixResults
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(PeertubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(PeertubeFilters.ID_SF_SENSITIVE_YES),
                "&nsfw=true&resultType=videos",
                null,
                null,
                fixResults
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(PeertubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(PeertubeFilters.ID_SF_SENSITIVE_NO),
                "&nsfw=false&resultType=videos",
                null,
                null,
                fixResults
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(PeertubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(PeertubeFilters.ID_SF_PUBLISHED_ALL),
                "&resultType=videos",
                null,
                null,
                fixResults
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(PeertubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(PeertubeFilters.ID_SF_PUBLISHED_TODAY),
                "&startDate=X&resultType=videos",
                null,
                null,
                fixResults
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(PeertubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(PeertubeFilters.ID_SF_PUBLISHED_LAST_7_DAYS),
                "&startDate=X&resultType=videos",
                null,
                null,
                fixResults
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(PeertubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(PeertubeFilters.ID_SF_PUBLISHED_LAST_30_DAYS),
                "&startDate=X&resultType=videos",
                null,
                null,
                fixResults
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(PeertubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(PeertubeFilters.ID_SF_PUBLISHED_LAST_YEAR),
                "&startDate=X&resultType=videos",
                null,
                null,
                fixResults
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(PeertubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(PeertubeFilters.ID_SF_DURATION_ALL),
                "&resultType=videos",
                null,
                null,
                fixResults
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(PeertubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(PeertubeFilters.ID_SF_DURATION_SHORT),
                "&durationMax=240&resultType=videos",
                null,
                null,
                fixResults
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(PeertubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(PeertubeFilters.ID_SF_DURATION_MEDIUM),
                "&durationMin=240&durationMax=600&resultType=videos",
                null,
                null,
                fixResults
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(PeertubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(PeertubeFilters.ID_SF_DURATION_LONG),
                "&durationMin=600&resultType=videos",
                null,
                null,
                fixResults
        ));

    }

    @Override
    protected void contentFiltersThatHaveCorrespondingSortFiltersTestSetup(
            final List<Integer> contentFiltersThatHaveCorrespondingSortFilters) {

        contentFiltersThatHaveCorrespondingSortFilters.add(PeertubeFilters.ID_CF_MAIN_VIDEOS);
    }

    /**
     * Here we test if ID_SF_SORT_ORDER_ASCENDING changes ID_SF_SORT_* accordingly.
     * <p>
     * <ul>
     * <li> In ascending mode we expect eg. ID_SF_SORT_BY_RELEVANCE to result
     *      in "&sort=match" and not in "&sort=-match".
     * <li> Additionally we test some other ID_SF_* to check that nothing changes there.
     * </ul>
     */
    @Test
    public void validSortFiltersWithAscendingTest() {
        final List<InputAndExpectedResultData> validSortFiltersWithAscending = new ArrayList<>();

        // set input and expected result data
        validSortFiltersWithAscending.add(new InputAndExpectedResultData(
                null,
                null,
                Arrays.asList(
                        PeertubeFilters.ID_SF_SORT_BY_RELEVANCE,
                        PeertubeFilters.ID_SF_SORT_ORDER_ASCENDING
                ),
                "&sort=match",
                null,
                null,
                fixResults
        ));
        validSortFiltersWithAscending.add(new InputAndExpectedResultData(
                null,
                null,
                Arrays.asList(
                        PeertubeFilters.ID_SF_SORT_BY_NAME,
                        PeertubeFilters.ID_SF_SORT_ORDER_ASCENDING
                ),
                "&sort=name",
                null,
                null,
                fixResults
        ));
        validSortFiltersWithAscending.add(new InputAndExpectedResultData(
                null,
                null,
                Arrays.asList(
                        PeertubeFilters.ID_SF_SORT_BY_DURATION,
                        PeertubeFilters.ID_SF_SORT_ORDER_ASCENDING
                ),
                "&sort=duration",
                null,
                null,
                fixResults
        ));
        validSortFiltersWithAscending.add(new InputAndExpectedResultData(
                null,
                null,
                Arrays.asList(
                        PeertubeFilters.ID_SF_SORT_BY_PUBLISH_DATE,
                        PeertubeFilters.ID_SF_SORT_ORDER_ASCENDING
                ),
                "&sort=publishedAt",
                null,
                null,
                fixResults
        ));
        validSortFiltersWithAscending.add(new InputAndExpectedResultData(
                null,
                null,
                Arrays.asList(
                        PeertubeFilters.ID_SF_SORT_BY_CREATION_DATE,
                        PeertubeFilters.ID_SF_SORT_ORDER_ASCENDING
                ),
                "&sort=createdAt",
                null,
                null,
                fixResults
        ));
        validSortFiltersWithAscending.add(new InputAndExpectedResultData(
                null,
                null,
                Arrays.asList(
                        PeertubeFilters.ID_SF_SORT_BY_VIEWS,
                        PeertubeFilters.ID_SF_SORT_ORDER_ASCENDING
                ),
                "&sort=views",
                null,
                null,
                fixResults
        ));
        validSortFiltersWithAscending.add(new InputAndExpectedResultData(
                null,
                null,
                Arrays.asList(
                        PeertubeFilters.ID_SF_SORT_BY_LIKES,
                        PeertubeFilters.ID_SF_SORT_ORDER_ASCENDING
                ),
                "&sort=likes",
                null,
                null,
                fixResults
        ));
        validSortFiltersWithAscending.add(new InputAndExpectedResultData(
                null,
                null,
                Arrays.asList(
                        PeertubeFilters.ID_SF_KIND_LIVE,
                        PeertubeFilters.ID_SF_SORT_ORDER_ASCENDING
                ),
                "&isLive=true",
                null,
                null,
                fixResults
        ));
        validSortFiltersWithAscending.add(new InputAndExpectedResultData(
                null,
                null,
                Arrays.asList(
                        PeertubeFilters.ID_SF_SENSITIVE_NO,
                        PeertubeFilters.ID_SF_SORT_ORDER_ASCENDING
                ),
                "&nsfw=false",
                null,
                null,
                fixResults
        ));
        validSortFiltersWithAscending.add(new InputAndExpectedResultData(
                null,
                null,
                Arrays.asList(
                        PeertubeFilters.ID_SF_PUBLISHED_LAST_YEAR,
                        PeertubeFilters.ID_SF_SORT_ORDER_ASCENDING
                ),
                "&startDate=X",
                null,
                null,
                fixResults
        ));
        validSortFiltersWithAscending.add(new InputAndExpectedResultData(
                null,
                null,
                Arrays.asList(
                        PeertubeFilters.ID_SF_DURATION_LONG,
                        PeertubeFilters.ID_SF_SORT_ORDER_ASCENDING
                ),
                "&durationMin=600",
                null,
                null,
                fixResults
        ));

        // run tests
        genericTester(validSortFiltersWithAscending, doNotCallAssertButShowResult);
    }

    @Test
    public void publishedDateCalculationTest() {
        final List<InputAndExpectedResultData> publishedDateCalculation = new ArrayList<>();
        publishedDateCalculation.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(PeertubeFilters.ID_SF_PUBLISHED_ALL),
                "",
                null,
                new SortFilterCheckDateCalculation(),
                fixResults
        ));
        publishedDateCalculation.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(PeertubeFilters.ID_SF_PUBLISHED_TODAY),
                "&startDate=X",
                null,
                new SortFilterCheckDateCalculation(),
                fixResults
        ));
        publishedDateCalculation.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(PeertubeFilters.ID_SF_PUBLISHED_LAST_7_DAYS),
                "&startDate=X",
                null,
                new SortFilterCheckDateCalculation(),
                fixResults
        ));
        publishedDateCalculation.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(PeertubeFilters.ID_SF_PUBLISHED_LAST_30_DAYS),
                "&startDate=X",
                null,
                new SortFilterCheckDateCalculation(),
                fixResults
        ));
        publishedDateCalculation.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(PeertubeFilters.ID_SF_PUBLISHED_LAST_YEAR),
                "&startDate=X",
                null,
                new SortFilterCheckDateCalculation(),
                fixResults
        ));

        genericTester(publishedDateCalculation, doNotCallAssertButShowResult);
    }

    private static class SortFilterCheckDateCalculation extends AdditionalResultChecker {

        private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS";

        @Override
        public void checkFilterItem(final FilterItem filterItem,
                                    final boolean showMode) {

            if (filterItem instanceof PeertubeFilters.PeertubePublishedDateFilterItem) {
                final PeertubeFilters.PeertubePublishedDateFilterItem dateFilterItem =
                        (PeertubeFilters.PeertubePublishedDateFilterItem) filterItem;

                final String expectedDate = calculateDate(getDays(dateFilterItem));

                if (!showMode) {
                    final String result = dateFilterItem.getQueryData();
                    if ("".equals(result)) { // the all case -> PeertubeFilters.ID_SF_PUBLISHED_ALL
                        assertTrue(result.contains(expectedDate));
                    } else {
                        try { // convert to millies since epoch to approx compare
                            final long expectedDateInMillis = parseDateGetEpochMillis(expectedDate);
                            final long resultDateInMillis = parseDateGetEpochMillis(result);
                            assertTrue(Math.abs(expectedDateInMillis - resultDateInMillis) < 2000);
                        } catch (final ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } else {
                    System.out.println("approx=\"" + expectedDate + "\"");
                }
            }
        }

        private String calculateDate(final int days) {
            if (days != PeertubeFilters.PeertubePublishedDateFilterItem.NO_DAYS_SET) {
                final LocalDateTime localDateTime = LocalDateTime.now().minusDays(days);

                return "startDate=" + localDateTime.format(
                        DateTimeFormatter.ofPattern(DATE_PATTERN));
            } else {
                return "";
            }
        }

        private long parseDateGetEpochMillis(final String inputDate) throws ParseException {
            // 10 = length of startDate=
            final String date = inputDate.substring(10);

            final SimpleDateFormat fmt = new SimpleDateFormat(DATE_PATTERN);
            return fmt.parse(date).getTime();
        }

        private int getDays(final PeertubeFilters.PeertubePublishedDateFilterItem item) {
            try {
                final Field days = item.getClass().getDeclaredField("days");
                days.setAccessible(true);
                return days.getInt(item);
            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
                throw new RuntimeException("could not find field \"days\"");
            }
        }
    }
}
