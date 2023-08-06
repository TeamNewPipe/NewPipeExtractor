package org.schabi.newpipe.extractor.services;

import org.junit.jupiter.api.Test;

public interface BaseExtractorTest {
    @Test
    void testServiceId() throws Exception;
    @Test
    void testName() throws Exception;
    @Test
    void testId() throws Exception;
    @Test
    void testUrl() throws Exception;
    @Test
    void testOriginalUrl() throws Exception;
}
