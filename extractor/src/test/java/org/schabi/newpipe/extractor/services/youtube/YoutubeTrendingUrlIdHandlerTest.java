package org.schabi.newpipe.extractor.services.youtube;

/*
 * Created by Christian Schabesberger on 12.08.17.
 *
 * Copyright (C) Christian Schabesberger 2017 <chris.schabesberger@mailbox.org>
 * YoutubeTrendingUrlIdHandlerTest.java is part of NewPipe.
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
import org.schabi.newpipe.extractor.UrlIdHandler;
import org.schabi.newpipe.extractor.services.youtube.urlIdHandlers.YoutubeTrendingUrlIdHandler;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

/**
 * Test for {@link YoutubeTrendingUrlIdHandler}
 */
public class YoutubeTrendingUrlIdHandlerTest {
    private static UrlIdHandler urlIdHandler;

    @BeforeClass
    public static void setUp() throws Exception {
        urlIdHandler = YouTube.getKioskList().getUrlIdHandlerByType("Trending");
        NewPipe.init(Downloader.getInstance());
    }

    @Test
    public void getUrl()
            throws Exception {
        assertEquals(urlIdHandler.setId("").getUrl(), "https://www.youtube.com/feed/trending");
    }

    @Test
    public void getId()
            throws Exception {
        assertEquals(urlIdHandler.setUrl("").getId(), "Trending");
    }

    @Test
    public void acceptUrl() {
        assertTrue(urlIdHandler.acceptUrl("https://www.youtube.com/feed/trending"));
        assertTrue(urlIdHandler.acceptUrl("https://www.youtube.com/feed/trending?adsf=fjaj#fhe"));
        assertTrue(urlIdHandler.acceptUrl("http://www.youtube.com/feed/trending"));
        assertTrue(urlIdHandler.acceptUrl("www.youtube.com/feed/trending"));
        assertTrue(urlIdHandler.acceptUrl("youtube.com/feed/trending"));
        assertTrue(urlIdHandler.acceptUrl("youtube.com/feed/trending?akdsakjf=dfije&kfj=dkjak"));
        assertTrue(urlIdHandler.acceptUrl("https://youtube.com/feed/trending"));
        assertTrue(urlIdHandler.acceptUrl("m.youtube.com/feed/trending"));

        assertFalse(urlIdHandler.acceptUrl("https://youtu.be/feed/trending"));
        assertFalse(urlIdHandler.acceptUrl("kdskjfiiejfia"));
        assertFalse(urlIdHandler.acceptUrl("https://www.youtube.com/bullshit/feed/trending"));
        assertFalse(urlIdHandler.acceptUrl("https://www.youtube.com/feed/trending/bullshit"));
        assertFalse(urlIdHandler.acceptUrl("https://www.youtube.com/feed/bullshit/trending"));
        assertFalse(urlIdHandler.acceptUrl("peter klaut aepferl youtube.com/feed/trending"));
        assertFalse(urlIdHandler.acceptUrl("youtube.com/feed/trending askjkf"));
        assertFalse(urlIdHandler.acceptUrl("askdjfi youtube.com/feed/trending askjkf"));
        assertFalse(urlIdHandler.acceptUrl("    youtube.com/feed/trending"));
        assertFalse(urlIdHandler.acceptUrl("https://www.youtube.com/feed/trending.html"));
        assertFalse(urlIdHandler.acceptUrl(""));
    }
}
