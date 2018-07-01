package org.schabi.newpipe.extractor.services.youtube;

/*
 * Created by Christian Schabesberger on 12.08.17.
 *
 * Copyright (C) Christian Schabesberger 2017 <chris.schabesberger@mailbox.org>
 * YoutubeTrendingUIHFactoryTest.java is part of NewPipe.
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
import org.schabi.newpipe.extractor.exceptions.FoundAdException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.uih.UIHFactory;
import org.schabi.newpipe.extractor.services.youtube.urlIdHandlers.YoutubeTrendingUIHFactory;

import java.text.ParseException;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

/**
 * Test for {@link YoutubeTrendingUIHFactory}
 */
public class YoutubeTrendingUIHFactoryTest {
    private static UIHFactory UIHFactory;

    @BeforeClass
    public static void setUp() throws Exception {
        UIHFactory = YouTube.getKioskList().getUrlIdHandlerByType("Trending");
        NewPipe.init(Downloader.getInstance());
    }

    @Test
    public void getUrl()
            throws Exception {
        assertEquals(UIHFactory.fromId("").getUrl(), "https://www.youtube.com/feed/trending");
    }

    @Test
    public void getId()
            throws Exception {
        assertEquals(UIHFactory.fromUrl("https://www.youtube.com/feed/trending").getId(), "Trending");
    }

    @Test
    public void acceptUrl() throws ParsingException {
        assertTrue(UIHFactory.acceptUrl("https://www.youtube.com/feed/trending"));
        assertTrue(UIHFactory.acceptUrl("https://www.youtube.com/feed/trending?adsf=fjaj#fhe"));
        assertTrue(UIHFactory.acceptUrl("http://www.youtube.com/feed/trending"));
        assertTrue(UIHFactory.acceptUrl("www.youtube.com/feed/trending"));
        assertTrue(UIHFactory.acceptUrl("youtube.com/feed/trending"));
        assertTrue(UIHFactory.acceptUrl("youtube.com/feed/trending?akdsakjf=dfije&kfj=dkjak"));
        assertTrue(UIHFactory.acceptUrl("https://youtube.com/feed/trending"));
        assertTrue(UIHFactory.acceptUrl("m.youtube.com/feed/trending"));

        assertFalse(UIHFactory.acceptUrl("https://youtu.be/feed/trending"));
        assertFalse(UIHFactory.acceptUrl("kdskjfiiejfia"));
        assertFalse(UIHFactory.acceptUrl("https://www.youtube.com/bullshit/feed/trending"));
        assertFalse(UIHFactory.acceptUrl("https://www.youtube.com/feed/trending/bullshit"));
        assertFalse(UIHFactory.acceptUrl("https://www.youtube.com/feed/bullshit/trending"));
        assertFalse(UIHFactory.acceptUrl("peter klaut aepferl youtube.com/feed/trending"));
        assertFalse(UIHFactory.acceptUrl("youtube.com/feed/trending askjkf"));
        assertFalse(UIHFactory.acceptUrl("askdjfi youtube.com/feed/trending askjkf"));
        assertFalse(UIHFactory.acceptUrl("    youtube.com/feed/trending"));
        assertFalse(UIHFactory.acceptUrl("https://www.youtube.com/feed/trending.html"));
        assertFalse(UIHFactory.acceptUrl(""));
    }
}
