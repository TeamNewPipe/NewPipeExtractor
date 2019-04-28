package org.schabi.newpipe.extractor.services.youtube;

/*
 * Created by Christian Schabesberger on 12.08.17.
 *
 * Copyright (C) Christian Schabesberger 2017 <chris.schabesberger@mailbox.org>
 * YoutubeTrendingLinkHandlerFactoryTest.java is part of NewPipe.
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
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeTrendingLinkHandlerFactory;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

/**
 * Test for {@link YoutubeTrendingLinkHandlerFactory}
 */
public class YoutubeTrendingLinkHandlerFactoryTest {
    private static LinkHandlerFactory LinkHandlerFactory;

    @BeforeClass
    public static void setUp() throws Exception {
        LinkHandlerFactory = YouTube.getKioskList().getListLinkHandlerFactoryByType("Trending");
        NewPipe.init(DownloaderTestImpl.getInstance());
    }

    @Test
    public void getUrl()
            throws Exception {
        assertEquals(LinkHandlerFactory.fromId("").getUrl(), "https://www.youtube.com/feed/trending");
    }

    @Test
    public void getId()
            throws Exception {
        assertEquals(LinkHandlerFactory.fromUrl("https://www.youtube.com/feed/trending").getId(), "Trending");
    }

    @Test
    public void acceptUrl() throws ParsingException {
        assertTrue(LinkHandlerFactory.acceptUrl("https://www.youtube.com/feed/trending"));
        assertTrue(LinkHandlerFactory.acceptUrl("https://www.youtube.com/feed/trending?adsf=fjaj#fhe"));
        assertTrue(LinkHandlerFactory.acceptUrl("http://www.youtube.com/feed/trending"));
        assertTrue(LinkHandlerFactory.acceptUrl("www.youtube.com/feed/trending"));
        assertTrue(LinkHandlerFactory.acceptUrl("youtube.com/feed/trending"));
        assertTrue(LinkHandlerFactory.acceptUrl("youtube.com/feed/trending?akdsakjf=dfije&kfj=dkjak"));
        assertTrue(LinkHandlerFactory.acceptUrl("https://youtube.com/feed/trending"));
        assertTrue(LinkHandlerFactory.acceptUrl("m.youtube.com/feed/trending"));

        assertTrue(LinkHandlerFactory.acceptUrl("https://www.invidio.us/feed/trending"));
        assertTrue(LinkHandlerFactory.acceptUrl("https://invidio.us/feed/trending"));
        assertTrue(LinkHandlerFactory.acceptUrl("invidio.us/feed/trending"));

        assertFalse(LinkHandlerFactory.acceptUrl("https://youtu.be/feed/trending"));
        assertFalse(LinkHandlerFactory.acceptUrl("kdskjfiiejfia"));
        assertFalse(LinkHandlerFactory.acceptUrl("https://www.youtube.com/bullshit/feed/trending"));
        assertFalse(LinkHandlerFactory.acceptUrl("https://www.youtube.com/feed/trending/bullshit"));
        assertFalse(LinkHandlerFactory.acceptUrl("https://www.youtube.com/feed/bullshit/trending"));
        assertFalse(LinkHandlerFactory.acceptUrl("peter klaut aepferl youtube.com/feed/trending"));
        assertFalse(LinkHandlerFactory.acceptUrl("youtube.com/feed/trending askjkf"));
        assertFalse(LinkHandlerFactory.acceptUrl("askdjfi youtube.com/feed/trending askjkf"));
        assertFalse(LinkHandlerFactory.acceptUrl("    youtube.com/feed/trending"));
        assertFalse(LinkHandlerFactory.acceptUrl("https://www.youtube.com/feed/trending.html"));
        assertFalse(LinkHandlerFactory.acceptUrl(""));
    }
}
