// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.youtube.search.filter;

import org.schabi.newpipe.extractor.search.filter.BaseSearchFilters;
import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.extractor.services.DefaultFiltersTest;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

class YoutubeFiltersTest extends DefaultFiltersTest {

    @Override
    protected BaseSearchFilters setupPriorTesting() {
        return new YoutubeFilters();
    }

    @Override
    protected String emptyContentFilterTestSetup(final BaseSearchFilters base) {
        base.setSelectedSortFilter(getAllSortFiltersList());
        return "https://www.youtube.com/results?search_query=test";
    }

    @Override
    protected void validContentFilterWithAllSortFiltersTestSetup(
            final List<InputAndExpectedResultData> validContentFiltersWithAllSortFilters) {

        // set all content filters and results
        validContentFiltersWithAllSortFilters.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_ALL),
                getAllSortFiltersIdsList(),
                "https://www.youtube.com/results?search_query=test&sp=CAMSGwgFGAIgASgBMAE4AUABcAF4AbgBAcgBAdABAQ%3D%3D",
                new CheckParam("CAMSGwgFGAIgASgBMAE4AUABcAF4AbgBAcgBAdABAQ%3D%3D"),
                null
        ));
        validContentFiltersWithAllSortFilters.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_VIDEOS),
                getAllSortFiltersIdsList(),
                "https://www.youtube.com/results?search_query=test&sp=CAMSHQgFEAEYAiABKAEwATgBQAFwAXgBuAEByAEB0AEB",
                new CheckParam("CAMSHQgFEAEYAiABKAEwATgBQAFwAXgBuAEByAEB0AEB"),
                null
        ));
        validContentFiltersWithAllSortFilters.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_CHANNELS),
                getAllSortFiltersIdsList(),
                "https://www.youtube.com/results?search_query=test&sp=CAMSHQgFEAIYAiABKAEwATgBQAFwAXgBuAEByAEB0AEB",
                new CheckParam("CAMSHQgFEAIYAiABKAEwATgBQAFwAXgBuAEByAEB0AEB"),
                null
        ));
        validContentFiltersWithAllSortFilters.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_PLAYLISTS),
                getAllSortFiltersIdsList(),
                "https://www.youtube.com/results?search_query=test&sp=CAMSHQgFEAMYAiABKAEwATgBQAFwAXgBuAEByAEB0AEB",
                new CheckParam("CAMSHQgFEAMYAiABKAEwATgBQAFwAXgBuAEByAEB0AEB"),
                null
        ));
        validContentFiltersWithAllSortFilters.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_YOUTUBE_MUSIC_SONGS),
                getAllSortFiltersIdsList(),
                "https://music.youtube.com/search?q=test",
                new CheckParam("Eg-KAQwIARAAGAAgACgAMABqChAEEAUQAxAKEAk%3D"),
                null
        ));
        validContentFiltersWithAllSortFilters.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_YOUTUBE_MUSIC_VIDEOS),
                getAllSortFiltersIdsList(),
                "https://music.youtube.com/search?q=test",
                new CheckParam("Eg-KAQwIABABGAAgACgAMABqChAEEAUQAxAKEAk%3D"),
                null
        ));
        validContentFiltersWithAllSortFilters.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_YOUTUBE_MUSIC_ALBUMS),
                getAllSortFiltersIdsList(),
                "https://music.youtube.com/search?q=test",
                new CheckParam("Eg-KAQwIABAAGAEgACgAMABqChAEEAUQAxAKEAk%3D"),
                null
        ));
        validContentFiltersWithAllSortFilters.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_YOUTUBE_MUSIC_PLAYLISTS),
                getAllSortFiltersIdsList(),
                "https://music.youtube.com/search?q=test",
                new CheckParam("Eg-KAQwIABAAGAAgACgBMABqChAEEAUQAxAKEAk%3D"),
                null
        ));
        validContentFiltersWithAllSortFilters.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_YOUTUBE_MUSIC_ARTISTS),
                getAllSortFiltersIdsList(),
                "https://music.youtube.com/search?q=test",
                new CheckParam("Eg-KAQwIABAAGAAgASgAMABqChAEEAUQAxAKEAk%3D"),
                null
        ));
    }

    @Override
    protected void validContentFilterSetup(
            final List<InputAndExpectedResultData> validContentFiltersAndExpectedResults) {

        validContentFiltersAndExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_ALL),
                null,
                "https://www.youtube.com/results?search_query=test&sp=EgA%3D",
                new CheckParam("EgA%3D"),
                null
        ));
        validContentFiltersAndExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_VIDEOS),
                null,
                "https://www.youtube.com/results?search_query=test&sp=EgIQAQ%3D%3D",
                new CheckParam("EgIQAQ%3D%3D"),
                null
        ));
        validContentFiltersAndExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_CHANNELS),
                null,
                "https://www.youtube.com/results?search_query=test&sp=EgIQAg%3D%3D",
                new CheckParam("EgIQAg%3D%3D"),
                null
        ));
        validContentFiltersAndExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_PLAYLISTS),
                null,
                "https://www.youtube.com/results?search_query=test&sp=EgIQAw%3D%3D",
                new CheckParam("EgIQAw%3D%3D"),
                null
        ));
        validContentFiltersAndExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_YOUTUBE_MUSIC_SONGS),
                null,
                "https://music.youtube.com/search?q=test",
                new CheckParam("Eg-KAQwIARAAGAAgACgAMABqChAEEAUQAxAKEAk%3D"),
                null
        ));
        validContentFiltersAndExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_YOUTUBE_MUSIC_VIDEOS),
                null,
                "https://music.youtube.com/search?q=test",
                new CheckParam("Eg-KAQwIABABGAAgACgAMABqChAEEAUQAxAKEAk%3D"),
                null
        ));
        validContentFiltersAndExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_YOUTUBE_MUSIC_ALBUMS),
                null,
                "https://music.youtube.com/search?q=test",
                new CheckParam("Eg-KAQwIABAAGAEgACgAMABqChAEEAUQAxAKEAk%3D"),
                null
        ));
        validContentFiltersAndExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_YOUTUBE_MUSIC_PLAYLISTS),
                null,
                "https://music.youtube.com/search?q=test",
                new CheckParam("Eg-KAQwIABAAGAAgACgBMABqChAEEAUQAxAKEAk%3D"),
                null
        ));
        validContentFiltersAndExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_YOUTUBE_MUSIC_ARTISTS),
                null,
                "https://music.youtube.com/search?q=test",
                new CheckParam("Eg-KAQwIABAAGAAgASgAMABqChAEEAUQAxAKEAk%3D"),
                null
        ));
    }

    @Override
    protected void validAllSortFilterSetup(
            final List<InputAndExpectedResultData> validAllSortFilters) {
        // no implementation here for youtube. As we cannot just have sort filters without
        // content filters
    }

    @Override
    protected void validContentFilterAllSortFiltersTestSetup(
            final List<InputAndExpectedResultData>
                    validContentFilterAllSortFiltersExpectedResults) {
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(YoutubeFilters.ID_SF_SORT_BY_RELEVANCE),
                "https://www.youtube.com/results?search_query=test&sp=CAASAhAB",
                new CheckParam("CAASAhAB"),
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(YoutubeFilters.ID_SF_SORT_BY_RATING),
                "https://www.youtube.com/results?search_query=test&sp=CAESAhAB",
                new CheckParam("CAESAhAB"),
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(YoutubeFilters.ID_SF_SORT_BY_DATE),
                "https://www.youtube.com/results?search_query=test&sp=CAISAhAB",
                new CheckParam("CAISAhAB"),
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(YoutubeFilters.ID_SF_SORT_BY_VIEWS),
                "https://www.youtube.com/results?search_query=test&sp=CAMSAhAB",
                new CheckParam("CAMSAhAB"),
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(YoutubeFilters.ID_SF_UPLOAD_DATE_ALL),
                "https://www.youtube.com/results?search_query=test&sp=EgIQAQ%3D%3D",
                new CheckParam("EgIQAQ%3D%3D"),
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(YoutubeFilters.ID_SF_UPLOAD_DATE_HOUR),
                "https://www.youtube.com/results?search_query=test&sp=EgQIARAB",
                new CheckParam("EgQIARAB"),
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(YoutubeFilters.ID_SF_UPLOAD_DATE_DAY),
                "https://www.youtube.com/results?search_query=test&sp=EgQIAhAB",
                new CheckParam("EgQIAhAB"),
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(YoutubeFilters.ID_SF_UPLOAD_DATE_WEEK),
                "https://www.youtube.com/results?search_query=test&sp=EgQIAxAB",
                new CheckParam("EgQIAxAB"),
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(YoutubeFilters.ID_SF_UPLOAD_DATE_MONTH),
                "https://www.youtube.com/results?search_query=test&sp=EgQIBBAB",
                new CheckParam("EgQIBBAB"),
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(YoutubeFilters.ID_SF_UPLOAD_DATE_YEAR),
                "https://www.youtube.com/results?search_query=test&sp=EgQIBRAB",
                new CheckParam("EgQIBRAB"),
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(YoutubeFilters.ID_SF_DURATION_ALL),
                "https://www.youtube.com/results?search_query=test&sp=EgIQAQ%3D%3D",
                new CheckParam("EgIQAQ%3D%3D"),
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(YoutubeFilters.ID_SF_DURATION_SHORT),
                "https://www.youtube.com/results?search_query=test&sp=EgQQARgB",
                new CheckParam("EgQQARgB"),
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(YoutubeFilters.ID_SF_DURATION_MEDIUM),
                "https://www.youtube.com/results?search_query=test&sp=EgQQARgD",
                new CheckParam("EgQQARgD"),
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(YoutubeFilters.ID_SF_DURATION_LONG),
                "https://www.youtube.com/results?search_query=test&sp=EgQQARgC",
                new CheckParam("EgQQARgC"),
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(YoutubeFilters.ID_SF_FEATURES_LIVE),
                "https://www.youtube.com/results?search_query=test&sp=EgQQAUAB",
                new CheckParam("EgQQAUAB"),
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(YoutubeFilters.ID_SF_FEATURES_4K),
                "https://www.youtube.com/results?search_query=test&sp=EgQQAXAB",
                new CheckParam("EgQQAXAB"),
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(YoutubeFilters.ID_SF_FEATURES_HD),
                "https://www.youtube.com/results?search_query=test&sp=EgQQASAB",
                new CheckParam("EgQQASAB"),
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(YoutubeFilters.ID_SF_FEATURES_SUBTITLES),
                "https://www.youtube.com/results?search_query=test&sp=EgQQASgB",
                new CheckParam("EgQQASgB"),
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(YoutubeFilters.ID_SF_FEATURES_CCOMMONS),
                "https://www.youtube.com/results?search_query=test&sp=EgQQATAB",
                new CheckParam("EgQQATAB"),
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(YoutubeFilters.ID_SF_FEATURES_360),
                "https://www.youtube.com/results?search_query=test&sp=EgQQAXgB",
                new CheckParam("EgQQAXgB"),
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(YoutubeFilters.ID_SF_FEATURES_VR180),
                "https://www.youtube.com/results?search_query=test&sp=EgUQAdABAQ%3D%3D",
                new CheckParam("EgUQAdABAQ%3D%3D"),
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(YoutubeFilters.ID_SF_FEATURES_3D),
                "https://www.youtube.com/results?search_query=test&sp=EgQQATgB",
                new CheckParam("EgQQATgB"),
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(YoutubeFilters.ID_SF_FEATURES_HDR),
                "https://www.youtube.com/results?search_query=test&sp=EgUQAcgBAQ%3D%3D",
                new CheckParam("EgUQAcgBAQ%3D%3D"),
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(YoutubeFilters.ID_SF_FEATURES_LOCATION),
                "https://www.youtube.com/results?search_query=test&sp=EgUQAbgBAQ%3D%3D",
                new CheckParam("EgUQAbgBAQ%3D%3D"),
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                "test",
                singletonList(YoutubeFilters.ID_CF_MAIN_VIDEOS),
                singletonList(YoutubeFilters.ID_SF_FEATURES_PURCHASED),
                "https://www.youtube.com/results?search_query=test&sp=EgQQAUgB",
                new CheckParam("EgQQAUgB"),
                null
        ));
    }

    @Override
    protected void contentFiltersThatHaveCorrespondingSortFiltersTestSetup(
            final List<Integer> contentFiltersThatHaveCorrespondingSortFilters) {
        contentFiltersThatHaveCorrespondingSortFilters.add(YoutubeFilters.ID_CF_MAIN_ALL);
        contentFiltersThatHaveCorrespondingSortFilters.add(YoutubeFilters.ID_CF_MAIN_VIDEOS);
        contentFiltersThatHaveCorrespondingSortFilters.add(YoutubeFilters.ID_CF_MAIN_CHANNELS);
        contentFiltersThatHaveCorrespondingSortFilters.add(YoutubeFilters.ID_CF_MAIN_PLAYLISTS);
    }

    static class CheckParam extends AdditionalResultChecker {
        private final String expectedResult;

        CheckParam(final String expectedResult) {
            this.expectedResult = expectedResult;
        }

        @Override
        public void checkFilterItem(final FilterItem filterItem,
                                    final boolean showMode) {
            if (filterItem instanceof YoutubeFilters.YoutubeContentFilterItem) {
                final YoutubeFilters.YoutubeContentFilterItem contentFilterItem =
                        (YoutubeFilters.YoutubeContentFilterItem) filterItem;
                if (showMode) {
                    System.out.println("extendedResult=\"" + contentFilterItem.getParams() + "\"");
                } else {
                    assertEquals(this.expectedResult, contentFilterItem.getParams());
                }
            } else {
                throw new RuntimeException("Illegal FilterItem found: " + filterItem.getClass());
            }
        }
    }
}
