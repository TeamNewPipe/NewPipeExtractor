package org.schabi.newpipe.extractor.services.youtube.invidious.extractors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.feed.FeedExtractor;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeFeedInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import javax.annotation.Nonnull;
import java.io.IOException;

/*
 * Copyright (C) 2020 Team NewPipe <tnp@newpipe.schabi.org>
 * InvidiousFeedExtractor.java is part of NewPipe Extractor.
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

public class InvidiousFeedExtractor extends FeedExtractor {

    private final String baseUrl;
    private Document document;

    public InvidiousFeedExtractor(StreamingService service, ListLinkHandler listLinkHandler) {
        super(service, listLinkHandler);
        this.baseUrl = service.getInstance().getUrl();
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() {
        final Elements entries = document.select("feed > entry");
        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

        for (Element entryElement : entries) {
            collector.commit(new YoutubeFeedInfoItemExtractor(entryElement));
            // no need for InvidiousFeedInfoItemExtractor, it's exactly the same structure
        }

        return new InfoItemsPage<>(collector, null);

    }

    @Override
    public String getNextPageUrl() throws IOException, ExtractionException {
        return null;
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(String pageUrl) throws IOException, ExtractionException {
        return null;
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        final String feedUrl = baseUrl + "/feed/channel/" + getLinkHandler().getId();

        final Response response = downloader.get(feedUrl);
        if (response.responseCode() >= 400) {
            throw new ExtractionException("Could not get page: " + response.responseCode() + " " + response.responseMessage());
        }

        document = Jsoup.parse(response.responseBody());
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return document.select("feed > author > name").first().text();
    }

    @Nonnull
    @Override
    public String getUrl() throws ParsingException {
        return document.select("feed > author > uri").first().text();
    }

    @Nonnull
    @Override
    public String getId() throws ParsingException {
        return document.getElementsByTag("yt:channelId").first().text();
    }

    @Override
    public boolean hasNextPage() throws IOException, ExtractionException {
        return false;
    }
}
