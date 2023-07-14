package org.schabi.newpipe.extractor.services;

import org.junit.jupiter.api.Test;

public interface BaseSearchExtractorTest extends BaseListExtractorTest {
    @Test
    void testSearchString() throws Exception;
    @Test
    void testSearchSuggestion() throws Exception;
    @Test
    void testSearchCorrected() throws Exception;
}
