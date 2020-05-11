package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.*;
import static org.schabi.newpipe.extractor.utils.JsonUtils.EMPTY_STRING;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

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
    private JsonObject videoInfo;
    private final TimeAgoParser timeAgoParser;
    private StreamType cachedStreamType;

    /**
     * Creates an extractor of StreamInfoItems from a YouTube page.
     *
     * @param videoInfoItem The JSON page element
     * @param timeAgoParser A parser of the textual dates or {@code null}.
     */
    public YoutubeStreamInfoItemExtractor(JsonObject videoInfoItem, @Nullable TimeAgoParser timeAgoParser) {
        this.videoInfo = videoInfoItem;
        this.timeAgoParser = timeAgoParser;
    }

    @Override
    public StreamType getStreamType() {
        if (cachedStreamType != null) {
            return cachedStreamType;
        }

        final JsonArray badges = videoInfo.getArray("badges");
        for (Object badge : badges) {
            if (((JsonObject) badge).getObject("metadataBadgeRenderer").getString("label", EMPTY_STRING).equals("LIVE NOW")) {
                return cachedStreamType = StreamType.LIVE_STREAM;
            }
        }

        final String style = videoInfo.getArray("thumbnailOverlays").getObject(0)
                .getObject("thumbnailOverlayTimeStatusRenderer").getString("style", EMPTY_STRING);
        if (style.equalsIgnoreCase("LIVE")) {
            return cachedStreamType = StreamType.LIVE_STREAM;
        }

        return cachedStreamType = StreamType.VIDEO_STREAM;
    }

    @Override
    public boolean isAd() throws ParsingException {
        return isPremium() || getName().equals("[Private video]") || getName().equals("[Deleted video]");
    }

    @Override
    public String getUrl() throws ParsingException {
        try {
            String videoId = videoInfo.getString("videoId");
            return YoutubeStreamLinkHandlerFactory.getInstance().getUrl(videoId);
        } catch (Exception e) {
            throw new ParsingException("Could not get url", e);
        }
    }

    @Override
    public String getName() throws ParsingException {
        String name = getTextFromObject(videoInfo.getObject("title"));
        if (!isNullOrEmpty(name)) return name;
        throw new ParsingException("Could not get name");
    }

    @Override
    public long getDuration() throws ParsingException {
        if (getStreamType() == StreamType.LIVE_STREAM || isPremiere()) {
            return -1;
        }

        String duration = getTextFromObject(videoInfo.getObject("lengthText"));

        if (isNullOrEmpty(duration)) {
            for (Object thumbnailOverlay : videoInfo.getArray("thumbnailOverlays")) {
                if (((JsonObject) thumbnailOverlay).has("thumbnailOverlayTimeStatusRenderer")) {
                    duration = getTextFromObject(((JsonObject) thumbnailOverlay)
                            .getObject("thumbnailOverlayTimeStatusRenderer").getObject("text"));
                }
            }

            if (isNullOrEmpty(duration)) throw new ParsingException("Could not get duration");
        }

        return YoutubeParsingHelper.parseDurationString(duration);
    }

    @Override
    public String getUploaderName() throws ParsingException {
        String name = getTextFromObject(videoInfo.getObject("longBylineText"));

        if (isNullOrEmpty(name)) {
            name = getTextFromObject(videoInfo.getObject("ownerText"));

            if (isNullOrEmpty(name)) {
                name = getTextFromObject(videoInfo.getObject("shortBylineText"));

                if (isNullOrEmpty(name)) throw new ParsingException("Could not get uploader name");
            }
        }

        return name;
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        String url = getUrlFromNavigationEndpoint(videoInfo.getObject("longBylineText")
                .getArray("runs").getObject(0).getObject("navigationEndpoint"));

        if (isNullOrEmpty(url)) {
            url = getUrlFromNavigationEndpoint(videoInfo.getObject("ownerText")
                    .getArray("runs").getObject(0).getObject("navigationEndpoint"));

            if (isNullOrEmpty(url)) {
                url = getUrlFromNavigationEndpoint(videoInfo.getObject("shortBylineText")
                        .getArray("runs").getObject(0).getObject("navigationEndpoint"));

                if (isNullOrEmpty(url)) throw new ParsingException("Could not get uploader url");
            }
        }

        return url;
    }

    @Nullable
    @Override
    public String getTextualUploadDate() throws ParsingException {
        if (getStreamType().equals(StreamType.LIVE_STREAM)) {
            return null;
        }

        if (isPremiere()) {
            final Date date = getDateFromPremiere().getTime();
            return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
        }

        final String publishedTimeText = getTextFromObject(videoInfo.getObject("publishedTimeText"));
        if (publishedTimeText != null && !publishedTimeText.isEmpty()) return publishedTimeText;

        return null;
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        if (getStreamType().equals(StreamType.LIVE_STREAM)) {
            return null;
        }

        if (isPremiere()) {
            return new DateWrapper(getDateFromPremiere());
        }

        final String textualUploadDate = getTextualUploadDate();
        if (timeAgoParser != null && !isNullOrEmpty(textualUploadDate)) {
            try {
                return timeAgoParser.parse(textualUploadDate);
            } catch (ParsingException e) {
                throw new ParsingException("Could not get upload date", e);
            }
        }
        return null;
    }

    @Override
    public long getViewCount() throws ParsingException {
        try {
            if (videoInfo.has("topStandaloneBadge") || isPremium()) {
                return -1;
            }

            if (!videoInfo.has("viewCountText")) {
                // This object is null when a video has its views hidden.
                return -1;
            }

            final String viewCount = getTextFromObject(videoInfo.getObject("viewCountText"));

            if (viewCount.toLowerCase().contains("no views")) {
                return 0;
            } else if (viewCount.toLowerCase().contains("recommended")) {
                return -1;
            }

            return Long.parseLong(Utils.removeNonDigitCharacters(viewCount));
        } catch (Exception e) {
            throw new ParsingException("Could not get view count", e);
        }
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        try {
            // TODO: Don't simply get the first item, but look at all thumbnails and their resolution
            String url = videoInfo.getObject("thumbnail").getArray("thumbnails")
                    .getObject(0).getString("url");

            return fixThumbnailUrl(url);
        } catch (Exception e) {
            throw new ParsingException("Could not get thumbnail url", e);
        }
    }

    private boolean isPremium() {
        JsonArray badges = videoInfo.getArray("badges");
        for (Object badge : badges) {
            if (((JsonObject) badge).getObject("metadataBadgeRenderer").getString("label", EMPTY_STRING).equals("Premium")) {
                return true;
            }
        }
        return false;
    }

    private boolean isPremiere() {
        return videoInfo.has("upcomingEventData");
    }

    private Calendar getDateFromPremiere() throws ParsingException {
        final JsonObject upcomingEventData = videoInfo.getObject("upcomingEventData");
        final String startTime = upcomingEventData.getString("startTime");

        try {
            final long startTimeTimestamp = Long.parseLong(startTime);
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(startTimeTimestamp * 1000L));
            return calendar;
        } catch (Exception e) {
            throw new ParsingException("Could not parse date from premiere:  \"" + startTime + "\"");
        }
    }
}
