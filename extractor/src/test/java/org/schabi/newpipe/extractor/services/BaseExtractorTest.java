package org.schabi.newpipe.extractor.services;

@SuppressWarnings("unused")
public interface BaseExtractorTest {
    void testServiceId() throws Exception;
    void testName() throws Exception;
    void testId() throws Exception;
    void testCleanUrl() throws Exception;
    void testOriginalUrl() throws Exception;
}
