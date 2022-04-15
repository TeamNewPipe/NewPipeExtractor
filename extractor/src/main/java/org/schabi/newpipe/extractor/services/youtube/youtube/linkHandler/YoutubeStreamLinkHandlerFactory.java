package org.schabi.newpipe.extractor.services.youtube.youtube.linkHandler;

import org.schabi.newpipe.extractor.services.youtube.shared.linkHandler.YoutubeLikeStreamLinkHandlerFactory;

/*
 * Created by Christian Schabesberger on 02.02.16.
 *
 * Copyright (C) Christian Schabesberger 2018 <chris.schabesberger@mailbox.org>
 * YoutubeStreamLinkHandlerFactory.java is part of NewPipe.
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

public final class YoutubeStreamLinkHandlerFactory extends YoutubeLikeStreamLinkHandlerFactory {

    private static final YoutubeStreamLinkHandlerFactory INSTANCE
            = new YoutubeStreamLinkHandlerFactory();

    private YoutubeStreamLinkHandlerFactory() {
    }

    public static YoutubeStreamLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public String getUrl(final String id) {
        return "https://www.youtube.com/watch?v=" + id;
    }
}
