package org.schabi.newpipe.extractor.services.soundcloud.search;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudSearchExtractor;
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudSearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeSearchExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;

/*
 * Created by Christian Schabesberger on 27.05.18
 *
 * Copyright (C) Christian Schabesberger 2018 <chris.schabesberger@mailbox.org>
 * YoutubeSearchExtractorStreamTest.java is part of NewPipe.
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

/**
 * Test for {@link YoutubeSearchExtractor}
 */
public class SoundcloudSearchExtractorDefaultTest extends SoundcloudSearchExtractorBaseTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        extractor = (SoundcloudSearchExtractor) SoundCloud.getSearchExtractor(
                new SoundcloudSearchQueryHandlerFactory().fromQuery("lill uzi vert",
                        Arrays.asList(new String[]{"tracks"}), ""));
        extractor.fetchPage();
        itemsPage = extractor.getInitialPage();
    }

    @Test
    public void testGetSecondPageUrl() throws Exception {
        assertEquals("https://api-v2.soundcloud.com/search/tracks?q=lill+uzi+vert&limit=10&offset=10",
                removeClientId(extractor.getNextPageUrl()));
    }

    @Test
    public void testResultListCheckIfContainsStreamItems() {
        boolean hasStreams = false;
        for(InfoItem item : itemsPage.getItems()) {
            if(item instanceof StreamInfoItem) {
                hasStreams = true;
            }
        }
        assertTrue("Has no InfoItemStreams", hasStreams);
    }

    @Test
    public void testGetSecondPage() throws Exception {
        SoundcloudSearchExtractor secondExtractor =
                (SoundcloudSearchExtractor) SoundCloud.getSearchExtractor("lill uzi vert");
        ListExtractor.InfoItemsPage<InfoItem> secondPage = secondExtractor.getPage(itemsPage.getNextPageUrl());
        assertTrue(Integer.toString(secondPage.getItems().size()),
                secondPage.getItems().size() >= 10);

        // check if its the same result
        boolean equals = true;
        for (int i = 0; i < secondPage.getItems().size()
                && i < itemsPage.getItems().size(); i++) {
            if(!secondPage.getItems().get(i).getUrl().equals(
                    itemsPage.getItems().get(i).getUrl())) {
                equals = false;
            }
        }
        assertFalse("First and second page are equal", equals);

        assertEquals("https://api-v2.soundcloud.com/search/tracks?q=lill+uzi+vert&limit=10&offset=20",
                removeClientId(secondPage.getNextPageUrl()));
    }


    @Test
    public void testId() throws Exception {
        assertEquals("lill uzi vert", extractor.getId());
    }

    @Test
    public void testName() {
        assertEquals("lill uzi vert", extractor.getName());
    }
}
