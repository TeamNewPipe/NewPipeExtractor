package org.schabi.newpipe.extractor.services.niconico;

import static org.junit.Assert.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.Niconico;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.Extractor;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.DefaultListExtractorTest;
import org.schabi.newpipe.extractor.services.niconico.extractors.NiconicoTrendExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.io.IOException;

public class NiconicoTrendExtractorTest extends DefaultListExtractorTest {
    private static NiconicoTrendExtractor extractor;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = (NiconicoTrendExtractor) Niconico.getKioskList()
                .getExtractorById("Trending", null);
        extractor.fetchPage();
    }

    @Test
    public void testVideoInfo() {
        final ListExtractor.InfoItemsPage<StreamInfoItem> items;
        try {
            items = extractor.getInitialPage();
            assertEquals("第1位：【Among Us】殺人欲旺盛な宇宙人狼 2021/07/18 ふにんがす 第4戦【VOICEROID実況】",
                    items.getItems().get(0).getName());
            assertEquals("https://www.nicovideo.jp/watch/sm39848591?ref=rss_specified_ranking_rss2",
                    items.getItems().get(0).getUrl());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ExtractionException e) {
            e.printStackTrace();
        }
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
