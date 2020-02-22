package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonObject;
import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.localization.TimeAgoParser;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nullable;

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

    public YoutubeStreamInfoItemExtractor(Element a, @Nullable TimeAgoParser timeAgoParser) {
        this.timeAgoParser = timeAgoParser;
    }

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
        try {
            if (videoInfo.getArray("badges").getObject(0).getObject("metadataBadgeRenderer").getString("label").equals("LIVE NOW")) {
                return StreamType.LIVE_STREAM;
            }
        } catch (Exception ignored) {}
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public boolean isAd() {
        return false;
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
        String name = null;
        try {
            name = videoInfo.getObject("title").getString("simpleText");
        } catch (Exception ignored) {}
        if (name == null) {
            try {
                name = videoInfo.getObject("title").getArray("runs").getObject(0).getString("text");
            } catch (Exception ignored) {}
        }
        if (name != null && !name.isEmpty()) return name;
        throw new ParsingException("Could not get name");
    }

    @Override
    public long getDuration() throws ParsingException {
        try {
            if (getStreamType() == StreamType.LIVE_STREAM) return -1;
            return YoutubeParsingHelper.parseDurationString(videoInfo.getObject("lengthText").getString("simpleText"));
        } catch (Exception e) {
            throw new ParsingException("Could not get duration", e);
        }
    }

    @Override
    public String getUploaderName() throws ParsingException {
        String name = null;
        try {
            name = videoInfo.getObject("longBylineText").getArray("runs")
                    .getObject(0).getString("text");
        } catch (Exception ignored) {}
        if (name == null) {
            try {
                name = videoInfo.getObject("ownerText").getArray("runs")
                        .getObject(0).getString("text");
            } catch (Exception ignored) {}
        }
        if (name != null && !name.isEmpty()) return name;
        throw new ParsingException("Could not get uploader name");
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        try {
            String id = null;
            try {
                id = videoInfo.getObject("longBylineText").getArray("runs")
                        .getObject(0).getObject("navigationEndpoint")
                        .getObject("browseEndpoint").getString("browseId");
            } catch (Exception ignored) {}
            if (id == null) {
                try {
                    id = videoInfo.getObject("ownerText").getArray("runs")
                            .getObject(0).getObject("navigationEndpoint")
                            .getObject("browseEndpoint").getString("browseId");
                } catch (Exception ignored) {}
            }
            if (id == null || id.isEmpty()) {
                throw new IllegalArgumentException("is empty");
            }
            return YoutubeChannelLinkHandlerFactory.getInstance().getUrl(id);
        } catch (Exception e) {
            throw new ParsingException("Could not get uploader url");
        }
    }

    @Nullable
    @Override
    public String getTextualUploadDate() {
        // TODO: Get upload date in case of a videoRenderer (not available in case of a compactVideoRenderer)
        return null;
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() {
        return null;
    }

    @Override
    public long getViewCount() throws ParsingException {
        try {
            String viewCount;
            if (getStreamType() == StreamType.LIVE_STREAM)  {
                viewCount = videoInfo.getObject("viewCountText")
                        .getArray("runs").getObject(0).getString("text");
            } else {
                viewCount = videoInfo.getObject("viewCountText").getString("simpleText");
            }
            if (viewCount.equals("Recommended for you")) return -1;
            return Long.parseLong(Utils.removeNonDigitCharacters(viewCount));
        } catch (Exception e) {
            throw new ParsingException("Could not get view count", e);
        }
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        try {
            // TODO: Don't simply get the first item, but look at all thumbnails and their resolution
            return videoInfo.getObject("thumbnail").getArray("thumbnails")
                    .getObject(0).getString("url");
        } catch (Exception e) {
            throw new ParsingException("Could not get thumbnail url", e);
        }
    }
}
