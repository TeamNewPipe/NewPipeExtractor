package org.schabi.newpipe.extractor.services;

@SuppressWarnings("unused")
public interface BasePlaylistExtractorTest extends BaseListExtractorTest {
    void testThumbnailUrl() throws Exception;
    void testBannerUrl() throws Exception;
    void testUploaderName() throws Exception;
    void testUploaderAvatarUrl() throws Exception;
    void testStreamCount() throws Exception;
}
