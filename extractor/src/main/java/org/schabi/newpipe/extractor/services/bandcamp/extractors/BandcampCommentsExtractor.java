package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.comments.CommentsExtractor;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.comments.CommentsInfoItemsCollector;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;

import javax.annotation.Nonnull;
import java.io.IOException;

public class BandcampCommentsExtractor extends CommentsExtractor {

    private Document document;


    public BandcampCommentsExtractor(StreamingService service, ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        String html = downloader.get(getLinkHandler().getUrl()).responseBody();
        document = Jsoup.parse(html);
    }

    @Nonnull
    @Override
    public InfoItemsPage<CommentsInfoItem> getInitialPage() throws IOException, ExtractionException {

        CommentsInfoItemsCollector collector = new CommentsInfoItemsCollector(getServiceId());

        Elements writings = document.getElementsByClass("writing");

        for (Element writing : writings) {
            collector.commit(new BandcampCommentsInfoItemExtractor(writing, getUrl()));
        }

        return new InfoItemsPage<>(collector, null);
    }

    @Override
    public InfoItemsPage<CommentsInfoItem> getPage(Page page) throws IOException, ExtractionException {
        return null;
    }
}
