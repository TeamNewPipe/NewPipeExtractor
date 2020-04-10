package org.schabi.newpipe.extractor.services.youtube.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.utils.Utils;

import java.net.URL;
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

public class YoutubeChannelLinkHandlerFactory extends ListLinkHandlerFactory {

    private static final YoutubeChannelLinkHandlerFactory instance = new YoutubeChannelLinkHandlerFactory();

    public static YoutubeChannelLinkHandlerFactory getInstance() {
        return instance;
    }

    /**
     * Returns URL to channel from an ID
     *
     * @param id Channel ID including e.g. 'channel/'
     * @param contentFilters
     * @param searchFilter
     * @return URL to channel
     */
    @Override
    public String getUrl(String id, List<String> contentFilters, String searchFilter) {
        return "https://www.youtube.com/" + id;
    }

    @Override
    public String getId(String url) throws ParsingException {
        try {
            URL urlObj = Utils.stringToURL(url);
            String path = urlObj.getPath();

            if (!Utils.isHTTP(urlObj) || !(YoutubeParsingHelper.isYoutubeURL(urlObj) ||
                    YoutubeParsingHelper.isInvidioURL(urlObj) || YoutubeParsingHelper.isHooktubeURL(urlObj))) {
                throw new ParsingException("the URL given is not a Youtube-URL");
            }

            if (!path.startsWith("/user/") && !path.startsWith("/channel/") && !path.startsWith("/c/")) {
                throw new ParsingException("the URL given is neither a channel nor an user");
            }

            // remove leading "/"
            path = path.substring(1);

            String[] splitPath = path.split("/");
            String id = splitPath[1];

            if (id == null || !id.matches("[A-Za-z0-9_-]+")) {
                throw new ParsingException("The given id is not a Youtube-Video-ID");
            }

            return splitPath[0] + "/" + id;
        } catch (final Exception exception) {
            throw new ParsingException("Error could not parse url :" + exception.getMessage(), exception);
        }
    }

    @Override
    public boolean onAcceptUrl(String url) {
        try {
            getId(url);
        } catch (ParsingException e) {
            return false;
        }
        return true;
    }
}
