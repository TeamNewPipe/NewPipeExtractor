package org.schabi.newpipe.extractor.services.youtube.invidious.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler;
import org.schabi.newpipe.extractor.search.InfoItemsSearchCollector;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousParsingHelper;

import javax.annotation.Nonnull;
import java.io.IOException;

/*
 * Copyright (C) 2020 Team NewPipe <tnp@newpipe.schabi.org>
 * InvidiousSearchExtractor.java is part of NewPipe Extractor.
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

public class InvidiousSearchExtractor extends SearchExtractor {

    private final String baseUrl;
    private JsonArray results;

    public InvidiousSearchExtractor(StreamingService service, SearchQueryHandler linkHandler) {
        super(service, linkHandler);
        baseUrl = service.getInstance().getUrl();
    }

    @Nonnull
    @Override
    public String getSearchSuggestion() throws ParsingException {
        return "";
    }

    @Override
    public boolean isCorrectedSearch() throws ParsingException {
        return false;
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws IOException, ExtractionException {
        final InfoItemsSearchCollector collector = new InfoItemsSearchCollector(getServiceId());
        for (Object o : results) {
            collectStreamsFrom(collector, (JsonObject) o);
        }

        return new InfoItemsPage<>(collector, getNextPageUrl());
    }

    @Override
    public String getNextPageUrl() throws IOException, ExtractionException {
        return getPageUrl(2);
    }

    @Override
    public InfoItemsPage<InfoItem> getPage(String pageUrl) throws IOException, ExtractionException {
        return null;
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        final Response response = downloader.get(getUrl());
        results = InvidiousParsingHelper.getValidJsonArrayFromResponse(response, getUrl());
    }

    private void collectStreamsFrom(InfoItemsSearchCollector collector, JsonObject json) {
        final String type = json.getString("type");

        switch (type) {
            case "video":
                collector.commit(new InvidiousStreamInfoItemExtractor(json, baseUrl));
                break;
            case "playlist":
                collector.commit(new InvidiousPlaylistInfoItemExtractor(json, baseUrl));
                break;
            case "channel":
                collector.commit(new InvidiousChannelInfoItemExtractor(json, baseUrl));
                break;
        }
    }

    private String getPageUrl(int page) throws ParsingException {
        return getUrl() + "&page=" + page;
    }
}
