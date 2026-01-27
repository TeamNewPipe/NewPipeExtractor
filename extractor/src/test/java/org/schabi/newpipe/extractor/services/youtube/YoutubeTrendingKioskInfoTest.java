package org.schabi.newpipe.extractor.services.youtube;

/*
 * Created by Christian Schabesberger on 12.08.17.
 *
 * Copyright (C) 2017 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * YoutubeTrendingKioskInfoTest.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor.  If not, see <http://www.gnu.org/licenses/>.
 */

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.kiosk.KioskInfo;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeLiveLinkHandlerFactory;

/**
 * Test for {@link KioskInfo}
 */
class YoutubeTrendingKioskInfoTest implements InitYoutubeTest {

    KioskInfo kioskInfo;

    @Override
    @BeforeAll
    public void setUp() throws Exception {
        InitYoutubeTest.super.setUp();

        final LinkHandlerFactory linkHandlerFactory = YouTube.getKioskList()
                .getListLinkHandlerFactoryByType(YoutubeLiveLinkHandlerFactory.KIOSK_ID);

        kioskInfo = KioskInfo.getInfo(YouTube,
                linkHandlerFactory.fromId(YoutubeLiveLinkHandlerFactory.KIOSK_ID).getUrl());
    }

    @Test
    void getStreams() {
        assertFalse(kioskInfo.getRelatedItems().isEmpty());
    }

    @Test
    void getId() {
        assertEquals(YoutubeLiveLinkHandlerFactory.KIOSK_ID, kioskInfo.getId());
    }

    @Test
    void getName() {
        assertFalse(kioskInfo.getName().isEmpty());
    }
}
