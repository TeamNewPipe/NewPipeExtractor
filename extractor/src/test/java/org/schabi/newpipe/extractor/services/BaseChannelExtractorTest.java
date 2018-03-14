package org.schabi.newpipe.extractor.services;

@SuppressWarnings("unused")
public interface BaseChannelExtractorTest extends BaseListExtractorTest {
    void testDescription() throws Exception;
    void testAvatarUrl() throws Exception;
    void testBannerUrl() throws Exception;
    void testFeedUrl() throws Exception;
    void testSubscriberCount() throws Exception;
}
