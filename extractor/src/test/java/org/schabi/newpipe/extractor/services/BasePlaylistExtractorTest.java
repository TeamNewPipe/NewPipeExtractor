package org.schabi.newpipe.extractor.services;

@SuppressWarnings("unused")
public interface BasePlaylistExtractorTest extends BaseListExtractorTest {
    void testThumbnail() throws Exception;
    void testBanner() throws Exception;
    void testUploaderName() throws Exception;
    void testUploaderAvatar() throws Exception;
    void testStreamCount() throws Exception;
}
