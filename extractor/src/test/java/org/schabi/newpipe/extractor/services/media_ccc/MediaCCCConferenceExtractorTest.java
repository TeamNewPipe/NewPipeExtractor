package org.schabi.newpipe.extractor.services.media_ccc;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCConferenceExtractor;
import org.schabi.newpipe.extractor.utils.Localization;

import static junit.framework.TestCase.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.MediaCCC;

/**
 * Test {@link MediaCCCConferenceExtractor}
 */
public class MediaCCCConferenceExtractorTest {
    private static ChannelExtractor extractor;

    @BeforeClass
    public static void setUpClass() throws Exception {
        NewPipe.init(Downloader.getInstance(), new Localization("en", "en_GB"));
        extractor = MediaCCC.getChannelExtractor("https://api.media.ccc.de/public/conferences/froscon2017");
        extractor.fetchPage();
    }

    @Test
    public void testName() throws Exception {
        assertEquals("FrOSCon 2017", extractor.getName());
    }

    @Test
    public void testGetUrl() throws Exception {
        assertEquals("https://api.media.ccc.de/public/conferences/froscon2017", extractor.getUrl());
    }

    @Test
    public void testGetOriginalUrl() throws Exception {
        assertEquals("https://media.ccc.de/c/froscon2017", extractor.getOriginalUrl());
    }

    @Test
    public void testGetThumbnailUrl() throws Exception {
        assertEquals("https://static.media.ccc.de/media/events/froscon/2017/logo.png", extractor.getAvatarUrl());
    }

    @Test
    public void testGetInitalPage() throws Exception {
        assertEquals(97,extractor.getInitialPage().getItems().size());
    }
}
