package org.schabi.newpipe.extractor.services;

import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
public interface BaseListExtractorTest extends BaseExtractorTest {
    @Test
    void testRelatedItems() throws Exception;
    @Test
    void testMoreRelatedItems() throws Exception;
}
