package org.schabi.newpipe.extractor.services.youtube;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.SuggestionExtractor;
import org.schabi.newpipe.extractor.UrlIdHandler;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.kiosk.KioskList;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.search.SearchEngine;
import org.schabi.newpipe.extractor.stream.StreamExtractor;

import java.io.IOException;


/*
 * Created by Christian Schabesberger on 23.08.15.
 *
 * Copyright (C) Christian Schabesberger 2015 <chris.schabesberger@mailbox.org>
 * YoutubeService.java is part of NewPipe.
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

public class YoutubeService extends StreamingService {

    public YoutubeService(int id, String name) {
        super(id, name);
    }

    @Override
    public SearchEngine getSearchEngine() {
        return new YoutubeSearchEngine(getServiceId());
    }

    @Override
    public UrlIdHandler getStreamUrlIdHandler() {
        return YoutubeStreamUrlIdHandler.getInstance();
    }

    @Override
    public UrlIdHandler getChannelUrlIdHandler() {
        return YoutubeChannelUrlIdHandler.getInstance();
    }

    @Override
    public UrlIdHandler getPlaylistUrlIdHandler() {
        return YoutubePlaylistUrlIdHandler.getInstance();
    }


    @Override
    public StreamExtractor getStreamExtractor(String url) throws IOException, ExtractionException {
        return new YoutubeStreamExtractor(this, url);
    }

    @Override
    public ChannelExtractor getChannelExtractor(String url, String nextStreamsUrl) throws IOException, ExtractionException {
        return new YoutubeChannelExtractor(this, url, nextStreamsUrl);
    }

    @Override
    public PlaylistExtractor getPlaylistExtractor(String url, String nextStreamsUrl) throws IOException, ExtractionException {
        return new YoutubePlaylistExtractor(this, url, nextStreamsUrl);
    }

    @Override
    public SuggestionExtractor getSuggestionExtractor() {
        return new YoutubeSuggestionExtractor(getServiceId());
    }

    @Override
    public KioskList getKioskList() throws ExtractionException {
        KioskList list = new KioskList(getServiceId());

        // add kiosks here e.g.:
        YoutubeTrendingUrlIdHandler h = new YoutubeTrendingUrlIdHandler();
        try {
            list.addKioskEntry(new YoutubeTrendingExtractor(this, h.getUrl(""), h.getUrl("")), h);
        } catch (Exception e) {
            throw new ExtractionException(e);
        }

        return list;
    }
}
