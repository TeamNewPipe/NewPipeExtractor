package org.schabi.newpipe.extractor.services.youtube.invidious.extractors;

import static org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousParsingHelper.getUid;
import static org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousParsingHelper.getValidResponseBody;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.feed.FeedExtractor;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousService;
import org.schabi.newpipe.extractor.services.youtube.youtube.extractors.YoutubeFeedInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import java.io.IOException;

import javax.annotation.Nonnull;

public class InvidiousFeedExtractor extends FeedExtractor {

    private final String baseUrl;
    private Document document;

    public InvidiousFeedExtractor(
            final InvidiousService service,
            final ListLinkHandler listLinkHandler
    ) {
        super(service, listLinkHandler);
        this.baseUrl = service.getInstance().getUrl();
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() {
        final Elements entries = document.select("feed > entry");
        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

        for (final Element entryElement : entries) {
            // no need for InvidiousFeedInfoItemExtractor, it's exactly the same structure
            collector.commit(new YoutubeFeedInfoItemExtractor(entryElement));
        }

        return new InfoItemsPage<>(collector, null);

    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(
            final Page page
    ) throws IOException, ExtractionException {
        return null;
    }

    @Override
    public void onFetchPage(
            @Nonnull final Downloader downloader
    ) throws IOException, ExtractionException {
        final String feedUrl = baseUrl + "/feed/channel/" + getUid(getLinkHandler().getId());
        final Response response = downloader.get(feedUrl);
        document = Jsoup.parse(getValidResponseBody(response, feedUrl));
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
}
