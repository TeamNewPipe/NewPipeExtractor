package org.schabi.newpipe.extractor.services;

import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
public interface BaseStreamExtractorTest extends BaseExtractorTest {
    @Test
    void testStreamType() throws Exception;
    @Test
    void testUploaderName() throws Exception;
    @Test
    void testUploaderUrl() throws Exception;
    @Test
    void testUploaderAvatars() throws Exception;
    @Test
    void testSubscriberCount() throws Exception;
    @Test
    void testSubChannelName() throws Exception;
    @Test
    void testSubChannelUrl() throws Exception;
    @Test
    void testSubChannelAvatars() throws Exception;
    @Test
    void testThumbnails() throws Exception;
    @Test
    void testDescription() throws Exception;
    @Test
    void testLength() throws Exception;
    @Test
    void testTimestamp() throws Exception;
    @Test
    void testViewCount() throws Exception;
    @Test
    void testUploadDate() throws Exception;
    @Test
    void testTextualUploadDate() throws Exception;
    @Test
    void testLikeCount() throws Exception;
    @Test
    void testDislikeCount() throws Exception;
    @Test
    void testRelatedItems() throws Exception;
    @Test
    void testAgeLimit() throws Exception;
    @Test
    void testErrorMessage() throws Exception;
    @Test
    void testAudioStreams() throws Exception;
    @Test
    void testVideoStreams() throws Exception;
    @Test
    void testSubtitles() throws Exception;
    @Test
    void testGetDashMpdUrl() throws Exception;
    @Test
    void testFrames() throws Exception;
    @Test
    void testHost() throws Exception;
    @Test
    void testPrivacy() throws Exception;
    @Test
    void testCategory() throws Exception;
    @Test
    void testLicence() throws Exception;
    @Test
    void testLanguageInfo() throws Exception;
    @Test
    void testTags() throws Exception;
    @Test
    void testSupportInfo() throws Exception;
}
