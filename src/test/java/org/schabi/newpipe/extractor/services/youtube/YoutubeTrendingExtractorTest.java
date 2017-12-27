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
import org.schabi.newpipe.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.stream.StreamInfoItemCollector;
import org.schabi.newpipe.extractor.utils.Utils;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertEmptyErrors;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;


/**
 * Test for {@link YoutubeTrendingUrlIdHandler}
 */
public class YoutubeTrendingExtractorTest {

    static YoutubeTrendingExtractor extractor;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(Downloader.getInstance());
        extractor = (YoutubeTrendingExtractor) YouTube.getService()
                .getKioskList()
                .getExtractorById("Trending", null);
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
    public void testGetStreams() throws Exception {
        StreamInfoItemCollector collector = extractor.getStreams();
        Utils.printErrors(collector);
        assertFalse("no streams are received", collector.getItemList().isEmpty());
    }

    @Test
    public void testGetStreamsErrors() throws Exception {
        assertEmptyErrors("errors during stream list extraction", extractor.getStreams().getErrors());
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
                || extractor.getNextStreams().getNextItemsList().isEmpty());
    }

    @Test
    public void testGetCleanUrl() throws Exception {
        assertEquals(extractor.getCleanUrl(), extractor.getCleanUrl(), "https://www.youtube.com/feed/trending");
    }
}
