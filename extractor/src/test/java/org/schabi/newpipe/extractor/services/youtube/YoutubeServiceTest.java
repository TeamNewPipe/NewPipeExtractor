package org.schabi.newpipe.extractor.services.youtube;

/*
 * Created by Christian Schabesberger on 29.12.15.
 *
 * Copyright (C) Christian Schabesberger 2015 <chris.schabesberger@mailbox.org>
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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.kiosk.KioskList;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeMixPlaylistExtractor;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubePlaylistExtractor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

/**
 * Test for {@link YoutubeService}
 */
public class YoutubeServiceTest {
    static StreamingService service;
    static KioskList kioskList;

    @BeforeAll
    public static void setUp() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        service = YouTube;
        kioskList = service.getKioskList();
    }

    @Test
    void testGetKioskAvailableKiosks() {
        assertFalse(kioskList.getAvailableKiosks().isEmpty(), "No kiosk got returned");
    }

    @Test
    void testGetDefaultKiosk() throws Exception {
        assertEquals(kioskList.getDefaultKioskExtractor(null).getId(), "Trending");
    }


    @Test
    void getPlayListExtractorIsNormalPlaylist() throws Exception {
        final PlaylistExtractor extractor = service.getPlaylistExtractor(
            "https://www.youtube.com/watch?v=JhqtYOnNrTs&list=PL-EkZZikQIQVqk9rBWzEo5b-2GeozElS");
        assertTrue(extractor instanceof YoutubePlaylistExtractor);
    }

    @Test
    void getPlaylistExtractorIsMix() throws Exception {
        final String videoId = "_AzeUSL9lZc";
        PlaylistExtractor extractor = YouTube.getPlaylistExtractor(
            "https://www.youtube.com/watch?v=" + videoId + "&list=RD" + videoId);
        assertTrue(extractor instanceof YoutubeMixPlaylistExtractor);

        extractor = YouTube.getPlaylistExtractor(
            "https://www.youtube.com/watch?v=" + videoId + "&list=RDMM" + videoId);
        assertTrue(extractor instanceof YoutubeMixPlaylistExtractor);

        final String mixVideoId = "qHtzO49SDmk";

        extractor = YouTube.getPlaylistExtractor(
            "https://www.youtube.com/watch?v=" + mixVideoId + "&list=RD" + videoId);
        assertTrue(extractor instanceof YoutubeMixPlaylistExtractor);
    }
}
