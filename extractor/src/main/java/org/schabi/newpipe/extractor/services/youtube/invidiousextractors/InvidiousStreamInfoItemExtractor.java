package org.schabi.newpipe.extractor.services.youtube.invidiousextractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.youtube.InvidiousInstance;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import javax.annotation.Nullable;

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

    private JsonObject json;
    private InvidiousInstance instance;

    public InvidiousStreamInfoItemExtractor(JsonObject json, InvidiousInstance instance) {
        this.json = json;
        this.instance = instance;
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        return null;
    }

    @Override
    public boolean isAd() throws ParsingException {
        return false;
    }

    @Override
    public long getDuration() {
        return json.getNumber("lengthSeconds").longValue();
    }

    @Override
    public long getViewCount() {
        return json.getNumber("viewCountText").longValue();
    }

    @Override
    public String getUploaderName() {
        return json.getString("author");
    }

    @Override
    public String getUploaderUrl() {
        final String url = json.getString("authorUrl");
        return url != null ? url : instance.getUrl() + "/channel/" + json.getString("authorId");
    }

    @Nullable
    @Override
    public String getTextualUploadDate() {
        return null;
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() {
        return null;
    }

    @Override
    public String getName() {
        return json.getString("title");
    }

    @Override
    public String getUrl() throws ParsingException {
        return instance.getUrl() + "/watch?v=" + json.getString("videoId");
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return null;
    }
}
