package org.schabi.newpipe.extractor.services.soundcloud.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.comments.CommentsInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper;

import java.util.Objects;

import javax.annotation.Nullable;

/*
 * Copyright (C) 2020 Team NewPipe <tnp@newpipe.schabi.org>
 * SoundcloudCommentsInfoItemExtractor.java is part of NewPipe Extractor.
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

public class SoundcloudCommentsInfoItemExtractor implements CommentsInfoItemExtractor {
    private JsonObject json;
    private String url;

    public SoundcloudCommentsInfoItemExtractor(JsonObject json, String url) {
        this.json = json;
        this.url = url;
    }

    @Override
    public String getCommentId() {
        return Objects.toString(json.getLong("id"), null);
    }

    @Override
    public String getCommentText() {
        return json.getString("body");
    }

    @Override
    public String getUploaderName() {
        return json.getObject("user").getString("username");
    }

    @Override
    public String getUploaderAvatarUrl() {
        return json.getObject("user").getString("avatar_url");
    }

    @Override
    public boolean getHeartedByUploader() throws ParsingException {
        return false;
    }

    @Override
    public String getUploaderUrl() {
        return json.getObject("user").getString("permalink_url");
    }

    @Override
    public String getTextualUploadDate() {
        return json.getString("created_at");
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return new DateWrapper(SoundcloudParsingHelper.parseDateFrom(getTextualUploadDate()));
    }

    @Override
    public int getLikeCount() {
        return -1;
    }

    @Override
    public String getName() throws ParsingException {
        return json.getObject("user").getString("permalink");
    }

    @Override
    public String getUrl() throws ParsingException {
        return url;
    }

    @Override
    public String getThumbnailUrl() {
        return json.getObject("user").getString("avatar_url");
    }
}
