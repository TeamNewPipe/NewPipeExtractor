package org.schabi.newpipe.extractor.services.youtube.extractors;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
    private final TimeAgoParser timeAgoParser;

    private String cachedUploadDate;

    /**
     * Creates an extractor of StreamInfoItems from a YouTube page.
     * @param item          The page element
     * @param timeAgoParser A parser of the textual dates or {@code null}.
     */
    public YoutubeStreamInfoItemExtractor(Element item, @Nullable TimeAgoParser timeAgoParser) {
        this.item = item;
        this.timeAgoParser = timeAgoParser;
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

    @Nullable
    @Override
    public String getTextualUploadDate() throws ParsingException {
        if (getStreamType().equals(StreamType.LIVE_STREAM)) {
            return null;
        }

        if (cachedUploadDate != null) {
            return cachedUploadDate;
        }

        try {
            if (isVideoReminder()) {
                final Calendar calendar = getDateFromReminder();
                if (calendar != null) {
                    return cachedUploadDate = new SimpleDateFormat("yyyy-MM-dd HH:mm")
                            .format(calendar.getTime());
                }
            }


            Element meta = item.select("div[class=\"yt-lockup-meta\"]").first();
            if (meta == null) return "";

            final Elements li = meta.select("li");
            if (li.isEmpty()) return "";

            return cachedUploadDate = li.first().text();
        } catch (Exception e) {
            throw new ParsingException("Could not get upload date", e);
        }
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        if (getStreamType().equals(StreamType.LIVE_STREAM)) {
            return null;
        }

        if (isVideoReminder()) {
            return new DateWrapper(getDateFromReminder());
        }

        String textualUploadDate = getTextualUploadDate();
        if (timeAgoParser != null && textualUploadDate != null && !textualUploadDate.isEmpty()) {
            return timeAgoParser.parse(textualUploadDate);
        } else {
            return null;
        }
    }

    @Override
    public long getViewCount() throws ParsingException {
        String input;

        final Element spanViewCount = item.select("span.view-count").first();
        if (spanViewCount != null) {
            input = spanViewCount.text();

        } else if (getStreamType().equals(StreamType.LIVE_STREAM)) {
            Element meta = item.select("ul.yt-lockup-meta-info").first();
            if (meta == null) return 0;

            final Elements li = meta.select("li");
            if (li.isEmpty()) return 0;

            input = li.first().text();
        } else {
            try {
                Element meta = item.select("div.yt-lockup-meta").first();
                if (meta == null) return -1;

                // This case can happen if google releases a special video
                if (meta.select("li").size() < 2) return -1;

                input = meta.select("li").get(1).text();
            } catch (IndexOutOfBoundsException e) {
                throw new ParsingException("Could not parse yt-lockup-meta although available: " + getUrl(), e);
            }
        }

        if (input == null) {
            throw new ParsingException("Input is null");
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


    private boolean isVideoReminder() {
        return !item.select("span.yt-uix-livereminder").isEmpty();
    }

    private Calendar getDateFromReminder() throws ParsingException {
        final Element timeFuture = item.select("span.yt-badge.localized-date").first();

        if (timeFuture == null) {
            throw new ParsingException("Span timeFuture is null");
        }

        final String timestamp = timeFuture.attr("data-timestamp");
        if (!timestamp.isEmpty()) {
            try {
                final Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date(Long.parseLong(timestamp) * 1000L));
                return calendar;
            } catch (Exception e) {
                throw new ParsingException("Could not parse = \"" + timestamp + "\"");
            }
        }

        throw new ParsingException("Could not parse date from reminder element: \"" + timeFuture + "\"");
    }

    /**
     * Generic method that checks if the element contains any clues that it's a livestream item
     */
    protected static boolean isLiveStream(Element item) {
        return !item.select("span[class*=\"yt-badge-live\"]").isEmpty()
                || !item.select("span[class*=\"video-time-overlay-live\"]").isEmpty();
    }
}
