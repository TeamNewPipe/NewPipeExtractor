package org.schabi.newpipe.extractor.services;

@SuppressWarnings("unused")
public interface BaseExtractorTest {
    void testServiceId();
    void testName() throws Exception;
    void testId() throws Exception;
    void testUrl() throws Exception;
    void testOriginalUrl();
}
