package org.schabi.newpipe.extractor.services.niconico;

import static org.schabi.newpipe.extractor.ServiceList.Niconico;

import org.junit.BeforeClass;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.Extractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.services.DefaultListExtractorTest;
import org.schabi.newpipe.extractor.services.niconico.extractors.NiconicoTrendExtractor;

public class NiconicoTrendExtractorTest extends DefaultListExtractorTest {
    private static NiconicoTrendExtractor extractor;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = (NiconicoTrendExtractor) Niconico.getKioskList()
                .getExtractorById("Trending", null);
        extractor.fetchPage();
    }

    @Override
    public Extractor extractor() throws Exception {
        return extractor;
    }

    @Override
    public StreamingService expectedService() throws Exception {
        return Niconico;
    }

    @Override
    public String expectedName() throws Exception {
        return "Trending";
    }

    @Override
    public String expectedId() throws Exception {
        return "Trending";
    }

    @Override
    public String expectedUrlContains() throws Exception {
        return "https://www.nicovideo.jp/ranking/genre/all?term=24h&rss=2.0";
    }

    @Override
    public String expectedOriginalUrlContains() throws Exception {
        return "https://www.nicovideo.jp/ranking/genre/all?term=24h&rss=2.0";
    }

    @Override
    public void testRelatedItems() throws Exception {
        // do nothing
    }

    @Override
    public boolean expectedHasMoreItems() {
        return false;
    }
}
