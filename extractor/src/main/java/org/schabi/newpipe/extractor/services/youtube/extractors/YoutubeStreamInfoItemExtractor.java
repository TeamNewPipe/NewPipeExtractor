package org.schabi.newpipe.extractor.services.youtube.extractors;

import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.utils.Utils;

/*
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * YoutubeStreamInfoItemExtractor.java is part of NewPipe.
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

public class YoutubeStreamInfoItemExtractor implements StreamInfoItemExtractor {

    private final Element item;

    public YoutubeStreamInfoItemExtractor(Element item) {
        this.item = item;
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        if (isLiveStream(item)) {
            return StreamType.LIVE_STREAM;
        } else {
            return StreamType.VIDEO_STREAM;
        }
    }

    @Override
    public boolean isAd() throws ParsingException {
        return !item.select("span[class*=\"icon-not-available\"]").isEmpty()
                || !item.select("span[class*=\"yt-badge-ad\"]").isEmpty()
                || isPremiumVideo();
    }

    private boolean isPremiumVideo() {
        Element premiumSpan = item.select("span[class=\"standalone-collection-badge-renderer-red-text\"]").first();
        if(premiumSpan == null) return false;

        // if this span has text it most likely says ("Free Video") so we can play this
        if(premiumSpan.hasText()) return false;
        return true;
    }

    @Override
    public String getUrl() throws ParsingException {
        try {
            Element el = item.select("div[class*=\"yt-lockup-video\"]").first();
            Element dl = el.select("h3").first().select("a").first();
            return dl.attr("abs:href");
        } catch (Exception e) {
            throw new ParsingException("Could not get web page url for the video", e);
        }
    }

    @Override
    public String getName() throws ParsingException {
        try {
            Element el = item.select("div[class*=\"yt-lockup-video\"]").first();
            Element dl = el.select("h3").first().select("a").first();
            return dl.text();
        } catch (Exception e) {
            throw new ParsingException("Could not get title", e);
        }
    }

    @Override
    public long getDuration() throws ParsingException {
        try {
            if (getStreamType() == StreamType.LIVE_STREAM) return -1;

            final Element duration = item.select("span[class*=\"video-time\"]").first();
            // apparently on youtube, video-time element will not show up if the video has a duration of 00:00
            // see: https://www.youtube.com/results?sp=EgIQAVAU&q=asdfgf
            return duration == null ? 0 : YoutubeParsingHelper.parseDurationString(duration.text());
        } catch (Exception e) {
            throw new ParsingException("Could not get Duration: " + getUrl(), e);
        }
    }

    @Override
    public String getUploaderName() throws ParsingException {
        try {
            return item.select("div[class=\"yt-lockup-byline\"]").first()
                    .select("a").first()
                    .text();
        } catch (Exception e) {
            throw new ParsingException("Could not get uploader", e);
        }
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        // this url is not always in the form "/channel/..."
        // sometimes Youtube provides urls in the from "/user/..."
        try {
            try {
                return item.select("div[class=\"yt-lockup-byline\"]").first()
                        .select("a").first()
                        .attr("abs:href");
            } catch (Exception e){}

            // try this if the first didn't work
            return item.select("span[class=\"title\"")
                    .text().split(" - ")[0];
        } catch (Exception e) {
            System.out.println(item.html());
            throw new ParsingException("Could not get uploader url", e);
        }
    }

    @Override
    public String getUploadDate() throws ParsingException {
        try {
            Element meta = item.select("div[class=\"yt-lockup-meta\"]").first();
            if (meta == null) return "";

            Element li = meta.select("li").first();
            if(li == null) return "";

            return meta.select("li").first().text();
        } catch (Exception e) {
            throw new ParsingException("Could not get upload date", e);
        }
    }

    @Override
    public long getViewCount() throws ParsingException {
        String input;
        try {
            // TODO: Return the actual live stream's watcher count
            // -1 for no view count
            if (getStreamType() == StreamType.LIVE_STREAM) return -1;

            Element meta = item.select("div[class=\"yt-lockup-meta\"]").first();
            if (meta == null) return -1;

            // This case can happen if google releases a special video
            if(meta.select("li").size() < 2)  return -1;

            input = meta.select("li").get(1).text();

        } catch (IndexOutOfBoundsException e) {
            throw new ParsingException("Could not parse yt-lockup-meta although available: " + getUrl(), e);
        }

        try {
            return Long.parseLong(Utils.removeNonDigitCharacters(input));
        } catch (NumberFormatException e) {
            // if this happens the video probably has no views
            if (!input.isEmpty()){
                return 0;
            }

            throw new ParsingException("Could not handle input: " + input, e);
        }
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        try {
            String url;
            Element te = item.select("div[class=\"yt-thumb video-thumb\"]").first()
                    .select("img").first();
            url = te.attr("abs:src");
            // Sometimes youtube sends links to gif files which somehow seem to not exist
            // anymore. Items with such gif also offer a secondary image source. So we are going
            // to use that if we've caught such an item.
            if (url.contains(".gif")) {
                url = te.attr("abs:data-thumb");
            }
            return url;
        } catch (Exception e) {
            throw new ParsingException("Could not get thumbnail url", e);
        }
    }

    /**
     * Generic method that checks if the element contains any clues that it's a livestream item
     */
    protected static boolean isLiveStream(Element item) {
        return !item.select("span[class*=\"yt-badge-live\"]").isEmpty()
                || !item.select("span[class*=\"video-time-overlay-live\"]").isEmpty();
    }
}
