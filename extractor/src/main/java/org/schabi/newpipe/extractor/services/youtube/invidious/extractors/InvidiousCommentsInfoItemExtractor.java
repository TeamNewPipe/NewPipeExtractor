package org.schabi.newpipe.extractor.services.youtube.invidious.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.comments.CommentsInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousParsingHelper;

import javax.annotation.Nullable;

/*
 * Copyright (C) 2020 Team NewPipe <tnp@newpipe.schabi.org>
 * InvidiousCommentsInfoItemExtractor.java is part of NewPipe Extractor.
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

public class InvidiousCommentsInfoItemExtractor implements CommentsInfoItemExtractor {

    private final JsonObject json;
    private final String url;

    public InvidiousCommentsInfoItemExtractor(final JsonObject json, final String url) {
        this.json = json;
        this.url = url;
    }

    @Override
    public int getLikeCount() {
        return json.getNumber("likeCount").intValue();
    }

    @Override
    public String getCommentText() {
        return json.getString("content");
    }

    @Override
    public String getTextualUploadDate() {
        return json.getString("publishedText");
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() {
        return InvidiousParsingHelper.getUploadDateFromEpochTime(json.getNumber("published").longValue());
    }

    @Override
    public String getCommentId() {
        return null; // unavailable
    }

    @Override
    public String getUploaderUrl() {
        return json.getString("authorUrl");
    }

    @Override
    public String getUploaderName() {
        return json.getString("author");
    }

    @Override
    public String getUploaderAvatarUrl() {
        return json.getArray("authorThumbnails").getObject(0).getString("url");
    }

    @Override
    public String getName() throws ParsingException {
        return json.getString("author");
    }

    @Override
    public String getUrl() throws ParsingException {
        return url;
    }

    @Override
    public String getThumbnailUrl() {
        final JsonArray thumbnail = json.getArray("authorThumbnails");
        return thumbnail.getObject(0).getString("url");
    }
}
