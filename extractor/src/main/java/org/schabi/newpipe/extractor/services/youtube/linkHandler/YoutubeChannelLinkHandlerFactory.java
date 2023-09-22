/*
 * Created by Christian Schabesberger on 25.07.16.
 *
 * Copyright (C) 2018 Christian Schabesberger <chrźis.schabesberger@mailbox.org>
 * YoutubeChannelLinkHandlerFactory.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor. If not, see <https://www.gnu.org/licenses/>.
 */

package org.schabi.newpipe.extractor.services.youtube.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nonnull;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

import static org.schabi.newpipe.extractor.utils.Utils.isBlank;

public final class YoutubeChannelLinkHandlerFactory extends ListLinkHandlerFactory {

    private static final YoutubeChannelLinkHandlerFactory INSTANCE
            = new YoutubeChannelLinkHandlerFactory();

    private static final Pattern EXCLUDED_SEGMENTS = Pattern.compile(
            // CHECKSTYLE:OFF
            "playlist|watch|attribution_link|watch_popup|embed|feed|select_site|account|reporthistory|redirect");
            // CHECKSTYLE:ON

    private YoutubeChannelLinkHandlerFactory() {
    }

    public static YoutubeChannelLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the URL to a channel from an ID.
     *
     * @param id the channel ID including e.g. 'channel/'
     * @return the URL to the channel
     */
    @Override
    public String getUrl(final String id,
                         final List<String> contentFilters,
                         final String searchFilter)
            throws ParsingException, UnsupportedOperationException {
        return "https://www.youtube.com/" + id;
    }

    /**
     * Checks whether the given path conforms to custom short channel URLs like
     * {@code youtube.com/yourcustomname}.
     *
     * @param splitPath the path segments array
     * @return whether the value conform to short channel URLs
     */
    private boolean isCustomShortChannelUrl(@Nonnull final String[] splitPath) {
        return splitPath.length == 1 && !EXCLUDED_SEGMENTS.matcher(splitPath[0]).matches();
    }

    /**
     * Checks whether the given path conforms to handle URLs like {@code youtube.com/@yourhandle}.
     *
     * @param splitPath the path segments array
     * @return whether the value conform to handle URLs
     */
    private boolean isHandle(@Nonnull final String[] splitPath) {
        return splitPath.length > 0 && splitPath[0].startsWith("@");
    }

    @Override
    public String getId(final String url) throws ParsingException, UnsupportedOperationException {
        try {
            final URL urlObj = Utils.stringToURL(url);
            String path = urlObj.getPath();

            if (!Utils.isHTTP(urlObj) || !(YoutubeParsingHelper.isYoutubeURL(urlObj)
                    || YoutubeParsingHelper.isInvidiousURL(urlObj)
                    || YoutubeParsingHelper.isHooktubeURL(urlObj))) {
                throw new ParsingException("The URL given is not a YouTube URL");
            }

            // Remove leading "/"
            path = path.substring(1);

            String[] splitPath = path.split("/");

            if (isHandle(splitPath)) {
                // Handle YouTube handle URLs like youtube.com/@yourhandle
                return splitPath[0];
            } else if (isCustomShortChannelUrl(splitPath)) {
                // Handle custom short channel URLs like youtube.com/yourcustomname
                path = "c/" + path;
                splitPath = path.split("/");
            }

            if (!path.startsWith("user/") && !path.startsWith("channel/")
                    && !path.startsWith("c/")) {
                throw new ParsingException(
                        "The given URL is not a channel, a user or a handle URL");
            }

            final String id = splitPath[1];

            if (isBlank(id)) {
                throw new ParsingException("The given ID is not a YouTube channel or user ID");
            }

            return splitPath[0] + "/" + id;
        } catch (final Exception e) {
            throw new ParsingException("Could not parse URL :" + e.getMessage(), e);
        }
    }

    @Override
    public boolean onAcceptUrl(final String url) {
        try {
            getId(url);
        } catch (final ParsingException e) {
            return false;
        }
        return true;
    }
}
