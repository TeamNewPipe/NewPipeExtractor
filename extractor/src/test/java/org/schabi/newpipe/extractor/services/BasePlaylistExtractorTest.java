package org.schabi.newpipe.extractor.services;

@SuppressWarnings("unused")
public interface BasePlaylistExtractorTest extends BaseListExtractorTest {
    void testThumbnails() throws Exception;
    void testBanners() throws Exception;
    void testUploaderName() throws Exception;
    void testUploaderAvatars() throws Exception;
    void testStreamCount() throws Exception;
}
