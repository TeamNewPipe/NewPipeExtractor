package org.schabi.newpipe.extractor.services.youtube.invidious.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousParsingHelper;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import javax.annotation.Nonnull;
import java.io.IOException;

/*
 * Copyright (C) 2020 Team NewPipe <tnp@newpipe.schabi.org>
 * InvidiousChannelExtractor.java is part of NewPipe Extractor.
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

public class InvidiousChannelExtractor extends ChannelExtractor {

    private final String baseUrl;
    private JsonObject json;

    public InvidiousChannelExtractor(StreamingService service, ListLinkHandler linkHandler) {
        super(service, linkHandler);
        this.baseUrl = service.getInstance().getUrl();
    }

    @Override
    public String getAvatarUrl() {
        return json.getArray("authorThumbnails").getObject(0).getString("url");
    }

    @Override
    public String getBannerUrl() {
        return json.getArray("authorBanners").getObject(0).getString("url");
    }

    @Override
    public String getFeedUrl() {
        return baseUrl + "/feed/channel/" + json.getString("authorId");
    }

    @Override
    public long getSubscriberCount() {
        return json.getNumber("subCount").longValue();
    }

    @Override
    public String getDescription() throws ParsingException {
        return json.getString("description");
    }

    @Override
    public String getParentChannelName() throws ParsingException {
        return null;
    }

    @Override
    public String getParentChannelUrl() throws ParsingException {
        return null;
    }

    @Override
    public String getParentChannelAvatarUrl() throws ParsingException {
        return null;
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws IOException, ExtractionException {
        final Downloader dl = NewPipe.getDownloader();
        final String apiUrl = getPageUrl(1);
        final Response rp = dl.get(apiUrl);
        final JsonArray array = InvidiousParsingHelper.getValidJsonArrayFromResponse(rp, apiUrl);

        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        collectStreamsFrom(collector, array);
        return new InfoItemsPage<>(collector, getNextPageUrl());
    }

    @Override
    public String getNextPageUrl() {
        return getPageUrl(2);
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(String pageUrl) throws IOException, ExtractionException {
        return null;
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        final String apiUrl = baseUrl + "/api/v1/channels/" + getId()
                + "?fields=author,description,subCount,authorThumbnails,authorBanners,authorId"
                + "&region=" + getExtractorContentCountry().getCountryCode();

        final Response response = downloader.get(apiUrl);

        json = InvidiousParsingHelper.getValidJsonObjectFromResponse(response, apiUrl);
    }

    @Nonnull
    @Override
    public String getName() {
        return json.getString("author");
    }

    private String getPageUrl(int page) {
        return baseUrl + "/api/v1/channels/videos/" + json.getString("authorId") + "?page=" + page;
    }

    private void collectStreamsFrom(final StreamInfoItemsCollector collector, final JsonArray videos) {
        for (Object o : videos) {
            collector.commit(new InvidiousStreamInfoItemExtractor((JsonObject) o, baseUrl));
        }
    }
}
