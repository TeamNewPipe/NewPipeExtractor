package org.schabi.newpipe.extractor.services.youtube.extractors;

import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.channel.ChannelInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Created by Christian Schabesberger on 12.02.17.
 *
 * Copyright (C) Christian Schabesberger 2017 <chris.schabesberger@mailbox.org>
 * YoutubeChannelInfoItemExtractor.java is part of NewPipe.
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

public class YoutubeChannelInfoItemExtractor implements ChannelInfoItemExtractor {
    private final Element el;

    public YoutubeChannelInfoItemExtractor(Element el) {
        this.el = el;
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        Element img = el.select("span[class*=\"yt-thumb-simple\"]").first()
                .select("img").first();

        String url = img.attr("abs:src");

        if (url.contains("gif")) {
            url = img.attr("abs:data-thumb");
        }
        return url;
    }

    @Override
    public String getName() throws ParsingException {
        return el.select("a[class*=\"yt-uix-tile-link\"]").first()
                .text();
    }

    @Override
    public String getUrl() throws ParsingException {
        try {
            String buttonTrackingUrl = el.select("button[class*=\"yt-uix-button\"]").first()
                    .attr("abs:data-href");

            Pattern channelIdPattern = Pattern.compile("(?:.*?)\\%252Fchannel\\%252F([A-Za-z0-9\\-\\_]+)(?:.*)");
            Matcher match = channelIdPattern.matcher(buttonTrackingUrl);

            if (match.matches()) {
                return YoutubeChannelExtractor.CHANNEL_URL_BASE + match.group(1);
            }
        } catch(Exception ignored) {}

        // fallback method for channels without "Subscribe" button (or just in case yt changes things)
        // provides an url with "/user/NAME", inconsistent with stream and channel extractor: tests will fail
        try {
            return el.select("a[class*=\"yt-uix-tile-link\"]").first()
                    .attr("abs:href");
        } catch (Exception e) {
            throw new ParsingException("Could not get channel url", e);
        }
    }

    @Override
    public long getSubscriberCount() throws ParsingException {
        final Element subsEl = el.select("span[class*=\"yt-subscriber-count\"]").first();
        if (subsEl != null) {
            try {
                return Long.parseLong(Utils.removeNonDigitCharacters(subsEl.text()));
            } catch (NumberFormatException e) {
                throw new ParsingException("Could not get subscriber count", e);
            }
        } else {
            // If the element is null, the channel have the subscriber count disabled
            return -1;
        }
    }

    @Override
    public long getStreamCount() throws ParsingException {
        Element metaEl = el.select("ul[class*=\"yt-lockup-meta-info\"]").first();
        if (metaEl == null) {
            return 0;
        } else {
            return Long.parseLong(Utils.removeNonDigitCharacters(metaEl.text()));
        }
    }

    @Override
    public String getDescription() throws ParsingException {
        Element desEl = el.select("div[class*=\"yt-lockup-description\"]").first();
        if (desEl == null) {
            return "";
        } else {
            return desEl.text();
        }
    }
}
