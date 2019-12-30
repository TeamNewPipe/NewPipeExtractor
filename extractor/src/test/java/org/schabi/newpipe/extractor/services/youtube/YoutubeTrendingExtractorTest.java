package org.schabi.newpipe.extractor.services.youtube;

/*
 * Created by Christian Schabesberger on 12.08.17.
 *
 * Copyright (C) Christian Schabesberger 2017 <chris.schabesberger@mailbox.org>
 * YoutubeTrendingExtractorTest.java is part of NewPipe.
 *
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.localization.ContentCountry;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeTrendingExtractor;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeTrendingLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.utils.Utils;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertEmptyErrors;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;


/**
 * Test for {@link YoutubeTrendingLinkHandlerFactory}
 */
public class YoutubeTrendingExtractorTest {

    static YoutubeTrendingExtractor extractor;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = (YoutubeTrendingExtractor) YouTube
                .getKioskList()
                .getExtractorById("Trending", null);
        extractor.forceContentCountry(new ContentCountry("de"));
        extractor.fetchPage();
    }

    @Test
    public void testGetDownloader() throws Exception {
        assertNotNull(NewPipe.getDownloader());
    }

    @Test
    public void testGetName() throws Exception {
        assertFalse(extractor.getName().isEmpty());
    }

    @Test
    public void testId() throws Exception {
        assertEquals(extractor.getId(), "Trending");
    }

    @Test
    public void testGetStreamsQuantity() throws Exception {
        ListExtractor.InfoItemsPage<StreamInfoItem> page = extractor.getInitialPage();
        Utils.printErrors(page.getErrors());
        assertTrue("no streams are received", page.getItems().size() >= 20);
    }

    @Test
    public void testGetStreamsErrors() throws Exception {
        assertEmptyErrors("errors during stream list extraction", extractor.getInitialPage().getErrors());
    }

    @Test
    public void testHasMoreStreams() throws Exception {
        // Setup the streams
        extractor.getInitialPage();
        assertFalse("has more streams", extractor.hasNextPage());
    }

    @Test
    public void testGetNextPage() {
        assertTrue("extractor has next streams", extractor.getPage(extractor.getNextPageUrl()) == null
                || extractor.getPage(extractor.getNextPageUrl()).getItems().isEmpty());
    }

    @Test
    public void testGetUrl() throws Exception {
        assertEquals(extractor.getUrl(), extractor.getUrl(), "https://www.youtube.com/feed/trending");
    }
}
