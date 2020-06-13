package org.schabi.newpipe.extractor.services.youtube.invidious.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.InfoItemExtractor;

/*
 * Copyright (C) 2020 Team NewPipe <tnp@newpipe.schabi.org>
 * InvidiousChannelInfoItemExtractor.java is part of NewPipe Extractor.
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

public class InvidiousChannelInfoItemExtractor implements InfoItemExtractor {

    private final JsonObject json;
    private final String baseUrl;

    public InvidiousChannelInfoItemExtractor(final JsonObject json, final String baseUrl) {
        this.json = json;
        this.baseUrl = baseUrl;
    }

    @Override
    public String getName() {
        return json.getString("author");
    }

    @Override
    public String getUrl() {
        return baseUrl + "/channel/" + json.getString("authorId");
    }

    @Override
    public String getThumbnailUrl() {
        return json.getArray("authorThumbnails").getObject(0).getString("url");
    }
}
