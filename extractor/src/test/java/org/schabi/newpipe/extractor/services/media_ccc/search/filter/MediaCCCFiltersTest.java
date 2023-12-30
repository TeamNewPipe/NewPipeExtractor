// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.media_ccc.search.filter;

import org.schabi.newpipe.extractor.search.filter.BaseSearchFilters;
import org.schabi.newpipe.extractor.services.DefaultFiltersTest;

import java.util.List;

import static java.util.Collections.singletonList;

class MediaCCCFiltersTest extends DefaultFiltersTest {

    @Override
    protected BaseSearchFilters setupPriorTesting() {
        doNotCallAssertButShowResult = false;
        return new MediaCCCFilters();
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
                singletonList(MediaCCCFilters.ID_CF_MAIN_ALL),
                null,
                null,
                null,
                null,
                null
        ));
        validContentFiltersAndExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(MediaCCCFilters.ID_CF_MAIN_CONFERENCES),
                null,
                null,
                null,
                null,
                null
        ));
        validContentFiltersAndExpectedResults.add(new InputAndExpectedResultData(
                null,
                singletonList(MediaCCCFilters.ID_CF_MAIN_EVENTS),
                null,
                null,
                null,
                null,
                null
        ));
    }

    @Override
    protected void validContentFilterAllSortFiltersTestSetup(
            final List<InputAndExpectedResultData>
                    validContentFilterAllSortFiltersExpectedResults) {
        // we have no sort filters for this service
    }

    @Override
    protected void validAllSortFilterSetup(
            final List<InputAndExpectedResultData> validAllSortFilters) {
        // we have no sort filters for this service
    }

    @Override
    protected void validContentFilterWithAllSortFiltersTestSetup(
            final List<InputAndExpectedResultData> validContentFiltersWithExpectedResult) {
        // we have no sort filters for this service
    }

    @Override
    protected void contentFiltersThatHaveCorrespondingSortFiltersTestSetup(
            final List<Integer> contentFiltersThatHaveCorrespondingSortFilters) {
        // we have no sort filters for this service
    }
}
