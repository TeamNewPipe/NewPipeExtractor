// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.soundcloud.search.filter;

import org.schabi.newpipe.extractor.search.filter.BaseSearchFilters;
import org.schabi.newpipe.extractor.services.DefaultFiltersTest;

import java.util.List;

import static java.util.Collections.singletonList;

class SoundcloudFiltersTest extends DefaultFiltersTest {

    @Override
    protected BaseSearchFilters setupPriorTesting() {
        doNotCallAssertButShowResult = false;
        return new SoundcloudFilters();
    }

    @Override
    protected String genericTesterEvaluator(final InputAndExpectedResultData testData) {
        return "(CF)"
                + searchFilterBase.evaluateSelectedContentFilters()
                + "|(SF)"
                + searchFilterBase.evaluateSelectedSortFilters();
    }

    /**
     * There is no implementation for {@link BaseSearchFilters#evaluateSelectedFilters(String)}.
     * <p>
     * -> therefore expected result is null.
     *
     * @param base the object to set up.
     * @return null
     */
    @Override
    protected String emptyContentFilterTestSetup(
            final BaseSearchFilters base) {
        return null;
    }

    @Override
    protected void validContentFilterSetup(
            final List<InputAndExpectedResultData> validContentFiltersAndExpectedResults) {
        validContentFiltersAndExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(SoundcloudFilters.ID_CF_MAIN_ALL),
                null,
                "(CF)|(SF)",
                null,
                null,
                null
        ));
        validContentFiltersAndExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(SoundcloudFilters.ID_CF_MAIN_TRACKS),
                null,
                "(CF)/tracks|(SF)",
                null,
                null,
                null
        ));
        validContentFiltersAndExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(SoundcloudFilters.ID_CF_MAIN_USERS),
                null,
                "(CF)/users|(SF)",
                null,
                null,
                null
        ));
        validContentFiltersAndExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(SoundcloudFilters.ID_CF_MAIN_PLAYLISTS),
                null,
                "(CF)/playlists|(SF)",
                null,
                null,
                null
        ));
    }

    @Override
    protected void validContentFilterAllSortFiltersTestSetup(
            final List<InputAndExpectedResultData>
                    validContentFilterAllSortFiltersExpectedResults) {
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(SoundcloudFilters.ID_CF_MAIN_TRACKS),
                singletonList(SoundcloudFilters.ID_SF_DATE_ALL),
                "(CF)/tracks|(SF)",
                null,
                null,
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(SoundcloudFilters.ID_CF_MAIN_TRACKS),
                singletonList(SoundcloudFilters.ID_SF_DATE_LAST_HOUR),
                "(CF)/tracks|(SF)&filter.created_at=last_hour",
                null,
                null,
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(SoundcloudFilters.ID_CF_MAIN_TRACKS),
                singletonList(SoundcloudFilters.ID_SF_DATE_LAST_DAY),
                "(CF)/tracks|(SF)&filter.created_at=last_day",
                null,
                null,
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(SoundcloudFilters.ID_CF_MAIN_TRACKS),
                singletonList(SoundcloudFilters.ID_SF_DATE_LAST_WEEK),
                "(CF)/tracks|(SF)&filter.created_at=last_week",
                null,
                null,
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(SoundcloudFilters.ID_CF_MAIN_TRACKS),
                singletonList(SoundcloudFilters.ID_SF_DATE_LAST_MONTH),
                "(CF)/tracks|(SF)&filter.created_at=last_month",
                null,
                null,
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(SoundcloudFilters.ID_CF_MAIN_TRACKS),
                singletonList(SoundcloudFilters.ID_SF_DATE_LAST_YEAR),
                "(CF)/tracks|(SF)&filter.created_at=last_year",
                null,
                null,
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(SoundcloudFilters.ID_CF_MAIN_TRACKS),
                singletonList(SoundcloudFilters.ID_SF_DURATION_ALL),
                "(CF)/tracks|(SF)",
                null,
                null,
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(SoundcloudFilters.ID_CF_MAIN_TRACKS),
                singletonList(SoundcloudFilters.ID_SF_DURATION_SHORT),
                "(CF)/tracks|(SF)&filter.duration=short",
                null,
                null,
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(SoundcloudFilters.ID_CF_MAIN_TRACKS),
                singletonList(SoundcloudFilters.ID_SF_DURATION_MEDIUM),
                "(CF)/tracks|(SF)&filter.duration=medium",
                null,
                null,
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(SoundcloudFilters.ID_CF_MAIN_TRACKS),
                singletonList(SoundcloudFilters.ID_SF_DURATION_LONG),
                "(CF)/tracks|(SF)&filter.duration=long",
                null,
                null,
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(SoundcloudFilters.ID_CF_MAIN_TRACKS),
                singletonList(SoundcloudFilters.ID_SF_DURATION_EPIC),
                "(CF)/tracks|(SF)&filter.duration=epic",
                null,
                null,
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(SoundcloudFilters.ID_CF_MAIN_TRACKS),
                singletonList(SoundcloudFilters.ID_SF_LICENSE_ALL),
                "(CF)/tracks|(SF)",
                null,
                null,
                null
        ));
        validContentFilterAllSortFiltersExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(SoundcloudFilters.ID_CF_MAIN_TRACKS),
                singletonList(SoundcloudFilters.ID_SF_LICENSE_COMMERCE),
                "(CF)/tracks|(SF)&filter.license=to_modify_commercially",
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
                singletonList(SoundcloudFilters.ID_SF_DATE_ALL),
                "(CF)|(SF)",
                null,
                null,
                null
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(SoundcloudFilters.ID_SF_DATE_LAST_HOUR),
                "(CF)|(SF)&filter.created_at=last_hour",
                null,
                null,
                null
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(SoundcloudFilters.ID_SF_DATE_LAST_DAY),
                "(CF)|(SF)&filter.created_at=last_day",
                null,
                null,
                null
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(SoundcloudFilters.ID_SF_DATE_LAST_WEEK),
                "(CF)|(SF)&filter.created_at=last_week",
                null,
                null,
                null
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(SoundcloudFilters.ID_SF_DATE_LAST_MONTH),
                "(CF)|(SF)&filter.created_at=last_month",
                null,
                null,
                null
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(SoundcloudFilters.ID_SF_DATE_LAST_YEAR),
                "(CF)|(SF)&filter.created_at=last_year",
                null,
                null,
                null
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(SoundcloudFilters.ID_SF_DURATION_ALL),
                "(CF)|(SF)",
                null,
                null,
                null
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(SoundcloudFilters.ID_SF_DURATION_SHORT),
                "(CF)|(SF)&filter.duration=short",
                null,
                null,
                null
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(SoundcloudFilters.ID_SF_DURATION_MEDIUM),
                "(CF)|(SF)&filter.duration=medium",
                null,
                null,
                null
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(SoundcloudFilters.ID_SF_DURATION_LONG),
                "(CF)|(SF)&filter.duration=long",
                null,
                null,
                null
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(SoundcloudFilters.ID_SF_DURATION_EPIC),
                "(CF)|(SF)&filter.duration=epic",
                null,
                null,
                null
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(SoundcloudFilters.ID_SF_LICENSE_ALL),
                "(CF)|(SF)",
                null,
                null,
                null
        ));
        validAllSortFilters.add(new InputAndExpectedResultData(
                null,
                null,
                singletonList(SoundcloudFilters.ID_SF_LICENSE_COMMERCE),
                "(CF)|(SF)&filter.license=to_modify_commercially",
                null,
                null,
                null
        ));
    }

    @Override
    protected void validContentFilterWithAllSortFiltersTestSetup(
            final List<InputAndExpectedResultData> validContentFiltersWithExpectedResult) {
        validContentFiltersWithExpectedResult.add(new InputAndExpectedResultData(
                null,
                singletonList(SoundcloudFilters.ID_CF_MAIN_ALL),
                getAllSortFiltersIdsList(),
                "(CF)|(SF)&filter.created_at=last_hour&filter.created_at=last_day&filter.created_at=last_week&filter.created_at=last_month&filter.created_at=last_year&filter.duration=short&filter.duration=medium&filter.duration=long&filter.duration=epic&filter.license=to_modify_commercially",
                null,
                null,
                null
        ));
        validContentFiltersWithExpectedResult.add(new InputAndExpectedResultData(
                null,
                singletonList(SoundcloudFilters.ID_CF_MAIN_TRACKS),
                getAllSortFiltersIdsList(),
                "(CF)/tracks|(SF)&filter.created_at=last_hour&filter.created_at=last_day&filter.created_at=last_week&filter.created_at=last_month&filter.created_at=last_year&filter.duration=short&filter.duration=medium&filter.duration=long&filter.duration=epic&filter.license=to_modify_commercially",
                null,
                null,
                null
        ));
        validContentFiltersWithExpectedResult.add(new InputAndExpectedResultData(
                null,
                singletonList(SoundcloudFilters.ID_CF_MAIN_USERS),
                getAllSortFiltersIdsList(),
                "(CF)/users|(SF)&filter.created_at=last_hour&filter.created_at=last_day&filter.created_at=last_week&filter.created_at=last_month&filter.created_at=last_year&filter.duration=short&filter.duration=medium&filter.duration=long&filter.duration=epic&filter.license=to_modify_commercially",
                null,
                null,
                null
        ));
        validContentFiltersWithExpectedResult.add(new InputAndExpectedResultData(
                null,
                singletonList(SoundcloudFilters.ID_CF_MAIN_PLAYLISTS),
                getAllSortFiltersIdsList(),
                "(CF)/playlists|(SF)&filter.created_at=last_hour&filter.created_at=last_day&filter.created_at=last_week&filter.created_at=last_month&filter.created_at=last_year&filter.duration=short&filter.duration=medium&filter.duration=long&filter.duration=epic&filter.license=to_modify_commercially",
                null,
                null,
                null
        ));
    }

    @Override
    protected void contentFiltersThatHaveCorrespondingSortFiltersTestSetup(
            final List<Integer> contentFiltersThatHaveCorrespondingSortFilters) {
        contentFiltersThatHaveCorrespondingSortFilters.add(SoundcloudFilters.ID_CF_MAIN_TRACKS);
    }
}
