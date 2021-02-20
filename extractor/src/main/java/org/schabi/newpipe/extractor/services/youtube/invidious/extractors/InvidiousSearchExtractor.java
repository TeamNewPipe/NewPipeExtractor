package org.schabi.newpipe.extractor.services.youtube.invidious.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.MetaInfo;
import org.schabi.newpipe.extractor.Page;
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
import java.util.Collections;
import java.util.List;

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
    public List<MetaInfo> getMetaInfo() throws ParsingException {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws IOException, ExtractionException {
        final InfoItemsSearchCollector collector = new InfoItemsSearchCollector(getServiceId());
        for (Object o : results) {
            collectStreamsFrom(collector, (JsonObject) o);
        }

        final Page nextPage = new Page(getUrl() + "&page=" + 2);

        return new InfoItemsPage<>(collector, nextPage);
    }

    @Override
    public InfoItemsPage<InfoItem> getPage(Page page) throws IOException, ExtractionException {
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

}
