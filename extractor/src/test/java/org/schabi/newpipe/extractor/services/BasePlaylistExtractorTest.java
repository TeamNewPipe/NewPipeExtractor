package org.schabi.newpipe.extractor.services;

import org.junit.jupiter.api.Test;

public interface BasePlaylistExtractorTest extends BaseListExtractorTest {
    @Test
    void testThumbnails() throws Exception;
    @Test
    void testBanners() throws Exception;
    @Test
    void testUploaderName() throws Exception;
    @Test
    void testUploaderAvatars() throws Exception;
    @Test
    void testStreamCount() throws Exception;
    @Test
    void testUploaderVerified() throws Exception;
}
