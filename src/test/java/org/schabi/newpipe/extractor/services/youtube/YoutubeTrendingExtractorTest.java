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

import org.junit.Before;
import org.junit.Test;
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.InfoItemCollector;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;


/**
 * Test for {@link YoutubeTrendingUrlIdHandler}
 */
public class YoutubeTrendingExtractorTest {

    KioskExtractor extractor;

    @Before
    public void setUp() throws Exception {
        NewPipe.init(Downloader.getInstance());
        extractor = YouTube.getService()
                .getKioskList()
                .getExtractorByType("Trending", null);
        extractor.fetchPage();
    }

    @Test
    public void testGetDownloader() throws Exception {
        assertNotNull(NewPipe.getDownloader());
    }

    @Test
    public void testGetName() throws Exception {
        assertEquals(extractor.getName(), "Trending");
    }

    @Test
    public void testId() throws Exception {
        assertEquals(extractor.getId(), "Trending");
    }

    @Test
    public void testGetStreams() throws Exception {
        InfoItemCollector collector = extractor.getStreams();
        if(!collector.getErrors().isEmpty()) {
            System.err.println("----------");
            for(Throwable e : collector.getErrors()) {
                e.printStackTrace();
                System.err.println("----------");
            }
        }
        assertTrue("no streams are received",
                !collector.getItemList().isEmpty()
                        && collector.getErrors().isEmpty());
    }

    @Test
    public void testGetStreamsErrors() throws Exception {
        assertTrue("errors during stream list extraction", extractor.getStreams().getErrors().isEmpty());
    }

    @Test
    public void testHasMoreStreams() throws Exception {
        // Setup the streams
        extractor.getStreams();
        assertFalse("has more streams", extractor.hasMoreStreams());
    }

    @Test
    public void testGetNextStreams() throws Exception {
        assertTrue("extractor has next streams", extractor.getNextStreams() == null
                || extractor.getNextStreams().nextItemsList.isEmpty());
    }

    @Test
    public void testGetCleanUrl() throws Exception {
        assertEquals(extractor.getCleanUrl(), extractor.getCleanUrl(), "https://www.youtube.com/feed/trending");
    }
}
