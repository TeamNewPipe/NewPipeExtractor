package org.schabi.newpipe.extractor.services.youtube;

/*
 * Created by Christian Schabesberger on 12.08.17.
 *
 * Copyright (C) Christian Schabesberger 2017 <chris.schabesberger@mailbox.org>
 * YoutubeTrendingKioskInfoTest.java is part of NewPipe.
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
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.kiosk.KioskInfo;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

/**
 * Test for {@link KioskInfo}
 */
public class YoutubeTrendingKioskInfoTest {
    static KioskInfo kioskInfo;

    @BeforeClass
    public static void setUp()
            throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        StreamingService service = YouTube;
        LinkHandlerFactory LinkHandlerFactory = service.getKioskList().getListLinkHandlerFactoryByType("Trending");

        kioskInfo = KioskInfo.getInfo(YouTube, LinkHandlerFactory.fromId("Trending").getUrl());
    }

    @Test
    public void getStreams() {
        assertFalse(kioskInfo.getRelatedItems().isEmpty());
    }

    @Test
    public void getId() {
        assertTrue(kioskInfo.getId().equals("Trending")
                || kioskInfo.getId().equals("Trends"));
    }

    @Test
    public void getName() {
        assertFalse(kioskInfo.getName().isEmpty());
    }
}
