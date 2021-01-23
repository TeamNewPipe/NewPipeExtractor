package org.schabi.newpipe.extractor.services.youtube.invidious.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousParsingHelper;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import javax.annotation.Nonnull;
import java.io.IOException;

/*
 * Copyright (C) 2020 Team NewPipe <tnp@newpipe.schabi.org>
 * InvidiousPlaylistExtractor.java is part of NewPipe Extractor.
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

public class InvidiousPlaylistExtractor extends PlaylistExtractor {

    private JsonObject json;
    private final String baseUrl;

    public InvidiousPlaylistExtractor(StreamingService service, ListLinkHandler linkHandler) {
        super(service, linkHandler);
        this.baseUrl = service.getInstance().getUrl();
    }

    @Override
    public String getThumbnailUrl() {
        final JsonArray thumbnails = json.getArray("authorThumbnails");
        return thumbnails.getObject(0).getString("url");
    }

    @Override
    public String getBannerUrl() {
        return null; // should it be ""?
    }

    @Override
    public String getUploaderUrl() {
        return baseUrl + "/channel/" + json.getString("authorId");
    }

    @Override
    public String getUploaderName() {
        return json.getString("author");
    }

    @Override
    public String getUploaderAvatarUrl() {
        // TODO : better resolution / use standard way to get avatar url
        return json.getArray("authorThumbnails").getObject(0).getString("url");
    }

    @Override
    public long getStreamCount() {
        final Number number = json.getNumber("videoCount");
        return number == null ? -1 : number.longValue();
    }

    @Nonnull
    @Override
    public String getSubChannelName() {
        return "";
    }

    @Nonnull
    @Override
    public String getSubChannelUrl() {
        return "";
    }

    @Nonnull
    @Override
    public String getSubChannelAvatarUrl() {
        return "";
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws IOException, ExtractionException {
        final JsonArray videos = json.getArray("videos");

        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        collectStreamsFrom(collector, videos);
        return new InfoItemsPage<>(collector, null);
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(Page page) throws IOException, ExtractionException {
        return null;
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        final String apiUrl = baseUrl + "/api/v1/playlists/" + getId();

        final Response response = downloader.get(apiUrl);

        json = InvidiousParsingHelper.getValidJsonObjectFromResponse(response, apiUrl);
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return json.getString("title");
    }

    private void collectStreamsFrom(StreamInfoItemsCollector collector, JsonArray videos) {
        for (Object o : videos) {
            collector.commit(new InvidiousStreamInfoItemExtractor((JsonObject) o, baseUrl));
        }
    }
}