package org.schabi.newpipe.extractor.services.youtube.invidious.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nullable;

import static org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousParsingHelper.getUploadDateFromEpochTime;

/*
 * Copyright (C) 2020 Team NewPipe <tnp@newpipe.schabi.org>
 * InvidiousStreamInfoItemExtractor.java is part of NewPipe Extractor.
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

public class InvidiousStreamInfoItemExtractor implements StreamInfoItemExtractor {

    private final JsonObject json;
    private final String baseUrl;

    public InvidiousStreamInfoItemExtractor(JsonObject json, String baseUrl) {
        this.json = json;
        this.baseUrl = baseUrl;
    }

    @Override
    public StreamType getStreamType() {
        if (json.getBoolean("liveNow")) {
            return StreamType.LIVE_STREAM;
        } else {
            return StreamType.VIDEO_STREAM;
        }
    }

    @Override
    public boolean isAd() {
        return json.getBoolean("premium") /*|| json.getBoolean("paid")*/; // not sure about this one
    }

    @Override
    public long getDuration() {
        return json.getNumber("lengthSeconds").longValue();
    }

    @Override
    public long getViewCount() {
        final Number viewCount = json.getNumber("viewCountText");
        if (viewCount != null) {
            return viewCount.longValue();
        }

        final String viewCountText = json.getString("viewCountText");
        try {
            return Utils.mixedNumberWordToLong(viewCountText);
        } catch (ParsingException e) {
            return -1;
        }
    }

    @Override
    public String getUploaderName() {
        return json.getString("author");
    }

    @Override
    public String getUploaderUrl() {
        final String url = json.getString("authorUrl");
        return url != null ? url : baseUrl + "/channel/" + json.getString("authorId");
    }

    @Nullable
    @Override
    public String getTextualUploadDate() {
        return json.getString("publishedText");
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() {
        final Number epochTime = json.getNumber("published");
        if (epochTime != null) {
            return getUploadDateFromEpochTime(epochTime.longValue());
        }

        // maybe use getTextualUploadDate() BUT is unstable because it depends on instance localization
        // (or configuration? I mean servers' OS language. That's something to investigate).
        // then it won't always be English, and there is no way to know the language from the API
        // therefore we should check if the string contains "ago" -> english

        return null;
    }

    @Override
    public String getName() {
        return json.getString("title");
    }

    @Override
    public String getUrl() throws ParsingException {
        return baseUrl + "/watch?v=" + json.getString("videoId");
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return json.getArray("videoThumbnails").getObject(0).getString("url");
    }
}
