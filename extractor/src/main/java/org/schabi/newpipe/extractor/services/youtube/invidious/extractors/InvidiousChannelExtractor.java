package org.schabi.newpipe.extractor.services.youtube.invidious.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
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

import static org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousParsingHelper.getUid;

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
        return InvidiousParsingHelper.getThumbnailUrl(json.getArray("authorThumbnails"));
    }

    @Override
    public String getBannerUrl() {
        return InvidiousParsingHelper.getThumbnailUrl(json.getArray("authorBanners"));
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
    public String getDescription() {
        return json.getString("description");
    }

    @Override
    public String getParentChannelName() {
        return null;
    }

    @Override
    public String getParentChannelUrl() {
        return null;
    }

    @Override
    public String getParentChannelAvatarUrl() {
        return null;
    }

    @Override
    public boolean isVerified() throws ParsingException {
        return false;
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws IOException, ExtractionException {
        return getPage(getPage(1));
    }

    public Page getPage(int page) {
        return InvidiousParsingHelper.getPage(baseUrl + "/api/v1/channels/videos/" +
                json.getString("authorId"), page);
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(Page page) throws IOException, ExtractionException {
        final Downloader dl = NewPipe.getDownloader();
        final String apiUrl = page.getUrl();
        final Response rp = dl.get(apiUrl);
        final JsonArray array = InvidiousParsingHelper.getValidJsonArrayFromResponse(rp, apiUrl);

        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        collectStreamsFrom(collector, array);

        Page nextPage;
        if (array.size() < 59) {
            // max number of items per page
            // with Second it is 29 but next Page logic is not implemented

            nextPage = null;
        } else {
            nextPage = getPage(Integer.parseInt(page.getId()) + 1);
        }

        return new InfoItemsPage<>(collector, nextPage);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        final String apiUrl = baseUrl + "/api/v1/channels/" + getUid(getId())
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

    private void collectStreamsFrom(final StreamInfoItemsCollector collector, final JsonArray videos) {
        for (Object o : videos) {
            collector.commit(new InvidiousStreamInfoItemExtractor((JsonObject) o, baseUrl));
        }
    }
}
