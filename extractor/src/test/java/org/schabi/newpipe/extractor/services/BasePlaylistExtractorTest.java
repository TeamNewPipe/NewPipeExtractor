package org.schabi.newpipe.extractor.services;

import org.junit.jupiter.api.Test;

public interface BasePlaylistExtractorTest extends BaseListExtractorTest {
    @Test
    void testThumbnailUrl() throws Exception;
    @Test
    void testBannerUrl() throws Exception;
    @Test
    void testUploaderName() throws Exception;
    @Test
    void testUploaderAvatarUrl() throws Exception;
    @Test
    void testStreamCount() throws Exception;
    @Test
    void testUploaderVerified() throws Exception;
}
