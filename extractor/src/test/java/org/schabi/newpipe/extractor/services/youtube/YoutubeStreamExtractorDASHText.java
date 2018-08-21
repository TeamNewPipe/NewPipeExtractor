package org.schabi.newpipe.extractor.services.youtube;


/*
 * Created by Christian Schabesberger on 30.12.15.
 *
 * Copyright (C) Christian Schabesberger 2015 <chris.schabesberger@mailbox.org>
 * YoutubeVideoExtractorDefault.java is part of NewPipe.
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
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

/**
 * Test for {@link StreamExtractor}
 */
public class YoutubeStreamExtractorDASHText {
    private static StreamInfo info;

    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(Downloader.getInstance());
        info = StreamInfo.getInfo(YouTube, "https://www.youtube.com/watch?v=00Q4SUnVQK4");
    }

    @Test
    public void testGetDashMpd() {
        System.out.println(info.getDashMpdUrl());
        assertTrue(info.getDashMpdUrl(),
                info.getDashMpdUrl() != null && !info.getDashMpdUrl().isEmpty());
    }

    @Test
    public void testDashMpdParser() {
        assertEquals(0, info.getAudioStreams().size());
        assertEquals(0, info.getVideoOnlyStreams().size());
        assertEquals(4, info.getVideoStreams().size());
    }
}
