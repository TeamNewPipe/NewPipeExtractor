package org.schabi.newpipe.extractor.services.youtube.invidious.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.InfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

/*
 * Copyright (C) 2020 Team NewPipe <tnp@newpipe.schabi.org>
 * InvidiousPlaylistInfoItemExtractor.java is part of NewPipe Extractor.
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

public class InvidiousPlaylistInfoItemExtractor implements InfoItemExtractor {

    private final JsonObject json;
    private final String baseUrl;

    public InvidiousPlaylistInfoItemExtractor(final JsonObject json, final String baseUrl) {
        this.json = json;
        this.baseUrl = baseUrl;
    }

    @Override
    public String getName() {
        return json.getString("title");
    }

    @Override
    public String getUrl() {
        return baseUrl + "playlist?list=" + json.getString("playlistId");
    }

    @Override
    public String getThumbnailUrl() {
        return json.getArray("videos").getObject(0).getArray("videoThumbnails")
                .getObject(0).getString("url");
    }
}
