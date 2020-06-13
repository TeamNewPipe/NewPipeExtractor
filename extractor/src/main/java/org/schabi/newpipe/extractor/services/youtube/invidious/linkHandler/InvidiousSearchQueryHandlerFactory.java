package org.schabi.newpipe.extractor.services.youtube.invidious.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;

import java.util.List;

/*
 * Copyright (C) 2020 Team NewPipe <tnp@newpipe.schabi.org>
 * InvidiousPlaylistExtractor.java is part of NewPipe Extractor.
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
 * along with NewPipe Extractor.  If not, see <https://www.gnu.org/licenses/>.
 */

public class InvidiousSearchQueryHandlerFactory extends SearchQueryHandlerFactory {

    public static final String ALL = "all";
    public static final String VIDEOS = "videos";
    public static final String CHANNELS = "channels";
    public static final String PLAYLISTS = "playlists";

    private static String baseUrl;

    private InvidiousSearchQueryHandlerFactory(final String baseUrl) {
        InvidiousSearchQueryHandlerFactory.baseUrl = baseUrl;
    }

    public static InvidiousSearchQueryHandlerFactory getInstance(final String baseUrl) {
        return new InvidiousSearchQueryHandlerFactory(baseUrl);
    }

    @Override
    public String getUrl(String query, List<String> contentFilter, String sortFilter) throws ParsingException {
        String url = baseUrl + "/api/v1/search?q=" + query;

        if (contentFilter.size() > 0) {
            switch (contentFilter.get(0)) {
                case VIDEOS:
                    return url; // + "&type=video" it's the default type provided by invidious
                case CHANNELS:
                    return url + "&type=channel";
                case PLAYLISTS:
                    return url + "&type=playlist";
                case ALL:
                default:
                    break;
            }
        }

        return url + "&type=all";
    }


    @Override
    public String[] getAvailableContentFilter() {
        return new String[]{
                ALL,
                VIDEOS,
                CHANNELS,
                PLAYLISTS
        };
    }

}
