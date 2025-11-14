package org.schabi.newpipe.extractor.services.youtube;

/*
 * Created by Christian Schabesberger on 12.08.17.
 *
 * Copyright (C) 2017 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * YoutubeTrendingLinkHandlerFactoryTest.java is part of NewPipe Extractor.
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.schabi.newpipe.extractor.InitNewPipeTest;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;

/**
 * Test for {@link YoutubeTrendingLinkHandlerFactory}
 */
public class YoutubeTrendingLinkHandlerFactoryTest {
    private static LinkHandlerFactory linkHandlerFactory;

    @BeforeAll
    public static void setUp() throws Exception {
        InitNewPipeTest.initEmpty();
        linkHandlerFactory = YouTube.getKioskList().getListLinkHandlerFactoryByType("trending_podcasts_episodes");
    }

    @Test
    public void getUrl() throws Exception {
        assertEquals("https://www.youtube.com/podcasts/popularepisodes", linkHandlerFactory.fromId("").getUrl());
    }

    @Test
    public void getId() throws Exception {
        assertEquals("trending_podcasts_episodes", linkHandlerFactory.fromUrl("https://www.youtube.com/podcasts/popularepisodes").getId());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "https://www.youtube.com/podcasts/popularepisodes",
        "https://www.youtube.com/podcasts/popularepisodes?adsf=fjaj#fhe",
        "http://www.youtube.com/podcasts/popularepisodes",
        "www.youtube.com/podcasts/popularepisodes",
        "youtube.com/podcasts/popularepisodes",
        "youtube.com/podcasts/popularepisodes?akdsakjf=dfije&kfj=dkjak",
        "https://youtube.com/podcasts/popularepisodes",
        "m.youtube.com/podcasts/popularepisodes",
        // "https://www.invidio.us/feed/trending",
        // "https://invidio.us/feed/trending",
        // "invidio.us/feed/trending"
    })
    public void shouldAcceptUrl(final String url) throws ParsingException {
        assertTrue(linkHandlerFactory.acceptUrl(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "https://youtu.be/feed/trending",
        "kdskjfiiejfia",
        "https://www.youtube.com/bullshit/podcasts/popularepisodes",
        "https://www.youtube.com/podcasts/popularepisodes/bullshit",
        "https://www.youtube.com/podcasts/bullshit/popularepisodes",
        "peter klaut aepferl youtube.com/podcasts/popularepisodes",
        "youtube.com/podcasts/popularepisodes askjkf",
        "askdjfi youtube.com/podcasts/popularepisodes askjkf",
        "    youtube.com/podcasts/popularepisodes",
        "https://www.youtube.com/podcasts/popularepisodes.html",
        ""
    })
    public void shouldNotAcceptUrl(final String url) throws ParsingException {
        assertFalse(linkHandlerFactory.acceptUrl(url));
    }
}
