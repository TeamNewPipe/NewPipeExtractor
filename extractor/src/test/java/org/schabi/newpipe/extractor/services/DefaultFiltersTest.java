// Created by evermind-zz 2022, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.search.filter.BaseSearchFilters;
import org.schabi.newpipe.extractor.search.filter.FilterContainer;
import org.schabi.newpipe.extractor.search.filter.FilterGroup;
import org.schabi.newpipe.extractor.search.filter.FilterItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public abstract class DefaultFiltersTest {

    /**
     * Each derived class has to implement {@link #setupPriorTesting()}.
     * Its return value will initialize this variable.
     */
    protected BaseSearchFilters searchFilterBase;

    /**
     * Set the {@link #genericTester(List, boolean)} into result display mode instead of asserting.
     *
     * <ul>
     *   <li> setting to 'true' is useful for developing unit test that use the
     *        {@link #genericTester(List, boolean)} method
     *   <li> this should be set in derived classes in {@link #setupPriorTesting()}
     * </ul>
     */
    protected boolean doNotCallAssertButShowResult = false;

    /**
     * Setup the to be tested class. It has to be derived from {@link BaseSearchFilters}.
     * <p>
     * Additional set up should also occur here.
     *
     * @return the to be tested derived {@link BaseSearchFilters} object.
     */
    protected abstract BaseSearchFilters setupPriorTesting();

    /**
     * Setup for Test {@link #emptyContentFilterTest()}
     *
     * @param base the object to set up.
     * @return the expected string
     */
    protected abstract String emptyContentFilterTestSetup(BaseSearchFilters base);

    @BeforeEach
    void setUp() {
        searchFilterBase = setupPriorTesting();
    }

    @AfterEach
    void tearDown() {
        searchFilterBase = null;
    }

    /**
     * Test {@link BaseSearchFilters#evaluateSelectedFilters(String)} with no content filters set.
     */
    @Test
    public void emptyContentFilterTest() {
        final String expectation = emptyContentFilterTestSetup(searchFilterBase);
        final String result = searchFilterBase.evaluateSelectedFilters("test");
        assertEquals(expectation, result);
    }

    @Test
    public void noSortFilterItemShouldBeNullTest() {
        final List<FilterItem> sortFilters = getAllSortFiltersList();
        for (final FilterItem item : sortFilters) {
            assertNotNull(item);
            assertNotNull(item.getNameId());
        }
    }

    @Test
    public void noContentFilterItemShouldBeNullTest() {
        final List<FilterItem> contentFilters = getAllContentFiltersList();
        checkThatNoFilterItemAndItsNameIsNull(contentFilters);
    }

    @Test
    public void validContentFilterTest() {
        final List<InputAndExpectedResultData> cfInputAndExpectedData = new ArrayList<>();
        // get input and expected result data
        validContentFilterSetup(cfInputAndExpectedData);

        genericTester(cfInputAndExpectedData, doNotCallAssertButShowResult);
    }

    protected abstract void validContentFilterSetup(
            List<InputAndExpectedResultData> validContentFiltersAndExpectedResults);

    /**
     * {@link #validContentFilterAllSortFiltersTestSetup(List)}}
     */
    @Test
    public void validContentFilterAndSortFiltersTest() {
        final List<InputAndExpectedResultData> validSortFiltersAndExpectedResults =
                new ArrayList<>();
        validContentFilterAllSortFiltersTestSetup(validSortFiltersAndExpectedResults);

        genericTester(validSortFiltersAndExpectedResults, doNotCallAssertButShowResult);
    }

    /**
     * For the list with {@link InputAndExpectedResultData} each entry should contain the same
     * content filter but each entry a different sort filter.
     *
     * @param validContentFilterAllSortFiltersExpectedResults the list with data described above
     */
    protected abstract void validContentFilterAllSortFiltersTestSetup(
            List<InputAndExpectedResultData> validContentFilterAllSortFiltersExpectedResults);

    @Test
    public void validSortFiltersTest() {
        final List<InputAndExpectedResultData> validAllSortFilters = new ArrayList<>();
        validAllSortFilterSetup(validAllSortFilters);

        genericTester(validAllSortFilters, doNotCallAssertButShowResult);
    }

    protected abstract void validAllSortFilterSetup(
            List<InputAndExpectedResultData> validAllSortFilters);

    /**
     * {@link #validContentFilterWithAllSortFiltersTestSetup(List)}}
     */
    @Test
    public void validContentFilterWithAllSortFiltersTest() {
        final List<InputAndExpectedResultData> validContentFiltersWithExpectedResult =
                new ArrayList<>();
        validContentFilterWithAllSortFiltersTestSetup(
                validContentFiltersWithExpectedResult);

        genericTester(validContentFiltersWithExpectedResult, doNotCallAssertButShowResult);
    }

    /**
     * One {@link InputAndExpectedResultData} should consist of one content filter and all
     * sort filters possible. Next entry should have different content filter, etc.
     *
     * @param validContentFiltersWithExpectedResult the list with data described above
     */
    protected abstract void validContentFilterWithAllSortFiltersTestSetup(
            List<InputAndExpectedResultData> validContentFiltersWithExpectedResult);

    /**
     * Test if content filter that claim to have corresponding
     * sort filters are true to their claim.
     * <p>
     * We need to setup {@link #contentFiltersThatHaveCorrespondingSortFiltersTestSetup(List)}
     */
    @Test
    public void contentFiltersWithSortFilterTest() {
        final FilterContainer filterContainer = searchFilterBase.getContentFilters();

        final List<Integer> contentFiltersThatHaveCorrespondingSortFilters = new ArrayList<>();
        contentFiltersThatHaveCorrespondingSortFiltersTestSetup(
                contentFiltersThatHaveCorrespondingSortFilters);

        for (final FilterGroup cfGroup : filterContainer.getFilterGroups()) {
            if (!cfGroup.getFilterItems().isEmpty()) {
                // if below id is having a sortFilterVariant it should be the same
                // variant than the one placed as superset to this group
                final int id = cfGroup.getFilterItems().get(0).getIdentifier();
                if (contentFiltersThatHaveCorrespondingSortFilters.contains(id)) {
                    final FilterContainer sortFilterVariant =
                            searchFilterBase.getContentFilterSortFilterVariant(id);
                    assertEquals(sortFilterVariant, cfGroup.getAllSortFilters());
                }
            }

            for (final FilterItem item : cfGroup.getFilterItems()) {
                final FilterContainer sortFilterVariant =
                        searchFilterBase.getContentFilterSortFilterVariant(item.getIdentifier());
                if (contentFiltersThatHaveCorrespondingSortFilters.contains(item.getIdentifier())) {
                    assertNotNull(sortFilterVariant);
                    assertNotNull(cfGroup.getAllSortFilters());
                    checkThatNoFilterItemAndItsNameIsNull(getAllFiltersList(sortFilterVariant));
                    checkThatNoFilterItemAndItsNameIsNull(
                            getAllFiltersList(cfGroup.getAllSortFilters()));
                } else {
                    assertNull(sortFilterVariant);
                }
            }
        }
    }

    protected abstract void contentFiltersThatHaveCorrespondingSortFiltersTestSetup(
            List<Integer> contentFiltersThatHaveCorrespondingSortFilters);

    // helpers
    private void checkThatNoFilterItemAndItsNameIsNull(final List<FilterItem> filterItems) {
        for (final FilterItem item : filterItems) {
            assertNotNull(item);
            assertNotNull(item.getNameId());
        }
    }

    protected void generateAllSortFiltersList() {
        searchFilterBase.setSelectedSortFilter(getAllSortFiltersList());
    }

    protected List<FilterItem> getAllSortFiltersList() {
        final FilterContainer contentFilterContainer = searchFilterBase.getContentFilters();
        final List<FilterItem> filterItems = new ArrayList<>();
        // for all content filter groups count available corresponding sort filters
        for (final FilterGroup group : contentFilterContainer.getFilterGroups()) {
            final FilterContainer sortFilterContainer = group.getAllSortFilters();
            if (null != sortFilterContainer) {
                for (final FilterGroup filterGroup : sortFilterContainer.getFilterGroups()) {
                    filterItems.addAll(filterGroup.getFilterItems());
                }
            }
        }
        return filterItems;
    }

    protected List<Integer> getAllSortFiltersIdsList() {
        final FilterContainer contentFilterContainer = searchFilterBase.getContentFilters();
        final List<Integer> filterItems = new ArrayList<>();
        for (final FilterGroup group : contentFilterContainer.getFilterGroups()) {
            final FilterContainer sortFilterContainer = group.getAllSortFilters();
            if (null != sortFilterContainer) {
                for (final FilterGroup filterGroup : sortFilterContainer.getFilterGroups()) {
                    for (final FilterItem item : filterGroup.getFilterItems()) {
                        filterItems.add(item.getIdentifier());
                    }
                }
            }
        }
        return filterItems;
    }

    protected List<FilterItem> getAllContentFiltersList() {
        return getAllFiltersList(searchFilterBase.getContentFilters());
    }

    protected List<FilterItem> getAllFiltersList(final FilterContainer filterContainer) {
        final List<FilterItem> filterItemList = new ArrayList<>();
        for (final FilterGroup cfGroup : filterContainer.getFilterGroups()) {
            filterItemList.addAll(cfGroup.getFilterItems());
        }
        return filterItemList;
    }

    protected List<FilterItem> getAllFiltersList(final List<FilterContainer> filterContainers) {
        final List<FilterItem> filterItemList = new ArrayList<>();
        for (final FilterContainer filterContainer : filterContainers) {
            filterItemList.addAll(getAllFiltersList(filterContainer));
        }
        return filterItemList;
    }

    protected int getNoOfFilterItems(final List<FilterContainer> filterContainers) {
        return filterContainers.stream()
                .mapToInt(DefaultSearchExtractorTest::getNoOfFilterItems).sum();
    }

    /**
     * A generic test method to make testing the various combinations easier.
     *
     * @param inputAndExpectedResultDataList the input test and expected result data
     * @param showMode                       if true it will display the generated result. If false
     *                                       the asserts will be called (normal test behaviour)
     */
    protected void genericTester(
            final List<InputAndExpectedResultData> inputAndExpectedResultDataList,
            final boolean showMode) {

        for (final InputAndExpectedResultData testData : inputAndExpectedResultDataList) {

            // (1) create input filter data
            final List<FilterItem> contentFilterItems = new ArrayList<>();
            final List<FilterItem> sortFilterItems = new ArrayList<>();
            if (testData.testContentFilters != null) {
                for (final Integer id : testData.testContentFilters) {
                    contentFilterItems.add(searchFilterBase.getFilterItem(id));
                }
            }
            if (testData.testSortFilters != null) {
                for (final Integer id : testData.testSortFilters) {
                    sortFilterItems.add(searchFilterBase.getFilterItem(id));
                }
            }

            // (2) set input filter data
            searchFilterBase.setSelectedContentFilter(contentFilterItems);
            searchFilterBase.setSelectedSortFilter(sortFilterItems);

            // (3) evaluate data
            String result = genericTesterEvaluator(testData);
            if (testData.resultFixer != null) {
                result = testData.resultFixer.findAndReplace(result);
            }
            // (4.1) run additional tests for content filters if available
            if (testData.testContentFilters != null) {
                for (final Integer id : testData.testContentFilters) {
                    if (testData.additionalContentResultChecker != null) {
                        testData.additionalContentResultChecker
                                .checkFilterItem(searchFilterBase.getFilterItem(id), showMode);
                    }
                }
            }
            // (4.2) run additional tests for sort filters if available
            if (testData.testSortFilters != null) {
                for (final Integer id : testData.testSortFilters) {
                    if (testData.additionalSortResultChecker != null) {
                        testData.additionalSortResultChecker
                                .checkFilterItem(searchFilterBase.getFilterItem(id), showMode);
                    }
                }
            }
            if (showMode) {
                System.out.println("result=\"" + result + "\"");
            } else {
                assertEquals(testData.expectedResult, result);
            }
        }
    }

    /**
     * The actual evaluation of the filters.
     * <p>
     * Some services use different methods so override in the derived class.
     *
     * @param testData the input testdata
     * @return the result of the test
     */
    protected String genericTesterEvaluator(final InputAndExpectedResultData testData) {
        return searchFilterBase.evaluateSelectedFilters(testData.searchString);
    }

    protected abstract static class AdditionalResultChecker {
        public abstract void checkFilterItem(FilterItem filterItem, boolean showMode);
    }

    protected static class InputAndExpectedResultData {

        public final AdditionalResultChecker additionalContentResultChecker;
        public final AdditionalResultChecker additionalSortResultChecker;
        public final String searchString;
        public final List<Integer> testContentFilters;
        public final List<Integer> testSortFilters;
        /**
         * Note: if null this expectedResult will not be asserted. -> Test will pass.
         */
        public final String expectedResult;
        public final ResultFixer resultFixer;

        public InputAndExpectedResultData(
                final String searchString,
                final List<Integer> testContentFilters,
                final List<Integer> testSortFilters,
                final String expectedResult,
                final AdditionalResultChecker additionalContentResultChecker,
                final AdditionalResultChecker additionalSortResultChecker) {
            this(searchString,
                    testContentFilters,
                    testSortFilters,
                    expectedResult,
                    additionalContentResultChecker,
                    additionalSortResultChecker,
                    new ResultFixer());
        }

        public InputAndExpectedResultData(
                final String searchString,
                final List<Integer> testContentFilters,
                final List<Integer> testSortFilters,
                final String expectedResult,
                final AdditionalResultChecker additionalContentResultChecker,
                final AdditionalResultChecker additionalSortResultChecker,
                final ResultFixer resultFixer) {
            this.searchString = searchString;
            this.testContentFilters = testContentFilters;
            this.testSortFilters = testSortFilters;
            this.expectedResult = expectedResult;
            this.additionalContentResultChecker = additionalContentResultChecker;
            this.additionalSortResultChecker = additionalSortResultChecker;
            this.resultFixer = resultFixer;
        }
    }

    public static class ResultFixer {
        public Map<String, String> searchAndReplaceMap = new HashMap<>();

        /**
         * Make results match expected value.
         * <p>
         * Eg. Peertube calculates in {@link org.schabi.newpipe.extractor.services.peertube.search.filter.PeertubeFilters.PeertubePublishedDateFilterItem}
         * the date to filter when something was published. So we have each moment we call a test
         * problems with the expected results. This method tries to fix that by
         * reading {@link #searchAndReplaceMap} search and replace data.
         *
         * @param result the string which should be altered
         * @return the altered string
         */
        @SuppressWarnings("checkstyle:FinalParameters")
        String findAndReplace(String result) {
            for (final Map.Entry<String, String> entry : searchAndReplaceMap.entrySet()) {
                result = result.replaceAll(entry.getKey(), entry.getValue());
            }
            return result;
        }
    }
}
