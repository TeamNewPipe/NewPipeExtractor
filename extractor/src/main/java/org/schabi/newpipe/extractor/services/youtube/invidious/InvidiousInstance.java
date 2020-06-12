package org.schabi.newpipe.extractor.services.youtube.invidious;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.Instance;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.InvalidInstanceException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isHooktubeURL;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.isYoutubeURL;

/*
 * Copyright (C) 2020 Team NewPipe <tnp@newpipe.schabi.org>
 * InvidiousInstance.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
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
 * along with NewPipe Extractor.  If not, see <https://www.gnu.org/licenses/>.
 */

public class InvidiousInstance implements Instance {

    private final String url;
    private String name;

    private static final InvidiousInstance defaultInstance = new InvidiousInstance("https://invidio.us", "invidious");

    public InvidiousInstance(String url, String name) {
        this.url = url;
        this.name = name;
    }

    public InvidiousInstance(String url) {
        this(url, "invidious");
    }

    @Nullable
    @Override
    public String getName() {
        return name;
    }

    @Nonnull
    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public boolean isValid() {
        URL url = null;
        try {
            url = Utils.stringToURL(this.url);
        } catch (MalformedURLException e) {
            return false;
        }

        if (isYoutubeURL(url) || isHooktubeURL(url)) {
            return false;
        }

        try {
            fetchInstanceMetaData();
            return true;
        } catch (InvalidInstanceException e) {
            return false;
        }
    }

    @Override
    public void fetchInstanceMetaData() throws InvalidInstanceException {
        Downloader downloader = NewPipe.getDownloader();
        Response response = null;

        try {
            response = downloader.get(url + "/api/v1/stats?fields=software");
        } catch (ReCaptchaException | IOException e) {
            throw new InvalidInstanceException("unable to configure instance " + url, e);
        }

        if (response == null || Utils.isBlank(response.responseBody())) {
            throw new InvalidInstanceException("unable to configure instance " + url);
        }

        try {
            JsonObject json = JsonParser.object().from(response.responseBody());
            this.name = JsonUtils.getString(json, "software.name");
        } catch (JsonParserException | ParsingException e) {
            throw new InvalidInstanceException("unable to parse instance config", e);
        }
    }

    public static Instance getDefaultInstance() {
        return defaultInstance;
    }

}
