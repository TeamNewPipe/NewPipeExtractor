package org.schabi.newpipe.extractor.services;

@SuppressWarnings("unused")
public interface BaseSearchExtractorTest extends BaseListExtractorTest {
    void testSearchString() throws Exception;
    void testSearchSuggestion() throws Exception;
}
