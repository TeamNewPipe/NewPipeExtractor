package org.schabi.newpipe.extractor.services;

public interface BaseStreamExtractorTest extends BaseExtractorTest {
    void testStreamType() throws Exception;
    void testUploaderName() throws Exception;
    void testUploaderUrl() throws Exception;
    void testUploaderAvatarUrl() throws Exception;
    void testSubscriberCount() throws Exception;
    void testSubChannelName() throws Exception;
    void testSubChannelUrl() throws Exception;
    void testSubChannelAvatarUrl() throws Exception;
    void testThumbnailUrl() throws Exception;
    void testDescription() throws Exception;
    void testLength() throws Exception;
    void testTimestamp() throws Exception;
    void testViewCount() throws Exception;
    void testUploadDate() throws Exception;
    void testTextualUploadDate() throws Exception;
    void testLikeCount() throws Exception;
    void testDislikeCount() throws Exception;
    void testRelatedItems() throws Exception;
    void testAgeLimit() throws Exception;
    void testErrorMessage() throws Exception;
    void testAudioStreams() throws Exception;
    void testVideoStreams() throws Exception;
    void testSubtitles() throws Exception;
    void testGetDashMpdUrl() throws Exception;
    void testFrames() throws Exception;
    void testHost() throws Exception;
    void testPrivacy() throws Exception;
    void testCategory() throws Exception;
    void testLicence() throws Exception;
    void testLanguageInfo() throws Exception;
    void testTags() throws Exception;
    void testSupportInfo() throws Exception;
}
