package org.schabi.newpipe.extractor.services.youtube.youtube.linkHandler;

import org.schabi.newpipe.extractor.services.youtube.shared.linkHandler.YoutubeLikeChannelLinkHandlerFactory;

import java.util.List;

/*
 * Created by Christian Schabesberger on 25.07.16.
 *
 * Copyright (C) Christian Schabesberger 2018 <chrźis.schabesberger@mailbox.org>
 * YoutubeChannelLinkHandlerFactory.java is part of NewPipe.
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

public final class YoutubeChannelLinkHandlerFactory extends YoutubeLikeChannelLinkHandlerFactory {

    public static final YoutubeChannelLinkHandlerFactory INSTANCE
            = new YoutubeChannelLinkHandlerFactory();

    private YoutubeChannelLinkHandlerFactory() {
    }

    public static YoutubeChannelLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Returns URL to channel from an ID
     *
     * @param id Channel ID including e.g. 'channel/'
     * @return URL to channel
     */
    @Override
    public String getUrl(final String id,
                         final List<String> contentFilters,
                         final String searchFilter) {
        return "https://www.youtube.com/" + id;
    }
}
