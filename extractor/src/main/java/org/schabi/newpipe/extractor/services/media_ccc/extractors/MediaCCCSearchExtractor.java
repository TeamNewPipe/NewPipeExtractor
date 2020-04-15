package org.schabi.newpipe.extractor.services.media_ccc.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelInfoItem;
import org.schabi.newpipe.extractor.channel.ChannelInfoItemExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler;
import org.schabi.newpipe.extractor.search.InfoItemsSearchCollector;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.media_ccc.extractors.infoItems.MediaCCCStreamInfoItemExtractor;
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCConferencesListLinkHandlerFactory;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

import static org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCSearchQueryHandlerFactory.ALL;
import static org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCSearchQueryHandlerFactory.CONFERENCES;
import static org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCSearchQueryHandlerFactory.EVENTS;

public class MediaCCCSearchExtractor extends SearchExtractor {
    private JsonObject doc;
    private MediaCCCConferenceKiosk conferenceKiosk;

    public MediaCCCSearchExtractor(final StreamingService service,
                                   final SearchQueryHandler linkHandler) {
        super(service, linkHandler);
        try {
            conferenceKiosk = new MediaCCCConferenceKiosk(service,
                    new MediaCCCConferencesListLinkHandlerFactory().fromId("conferences"),
                    "conferences");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nonnull
    @Override
    public String getSearchSuggestion() {
        return "";
    }

    @Override
    public boolean isCorrectedSearch() {
        return false;
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() {
        final InfoItemsSearchCollector searchItems = new InfoItemsSearchCollector(getServiceId());

        if (getLinkHandler().getContentFilters().contains(CONFERENCES)
                || getLinkHandler().getContentFilters().contains(ALL)
                || getLinkHandler().getContentFilters().isEmpty()) {
            searchConferences(getSearchString(),
                    conferenceKiosk.getInitialPage().getItems(),
                    searchItems);
        }

        if (getLinkHandler().getContentFilters().contains(EVENTS)
                || getLinkHandler().getContentFilters().contains(ALL)
                || getLinkHandler().getContentFilters().isEmpty()) {
            JsonArray events = doc.getArray("events");
            for (int i = 0; i < events.size(); i++) {
                searchItems.commit(new MediaCCCStreamInfoItemExtractor(
                        events.getObject(i)));
            }
        }
        return new InfoItemsPage<>(searchItems, null);
    }

    @Override
    public InfoItemsPage<InfoItem> getPage(final Page page) {
        return InfoItemsPage.emptyPage();
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        if (getLinkHandler().getContentFilters().contains(EVENTS)
                || getLinkHandler().getContentFilters().contains(ALL)
                || getLinkHandler().getContentFilters().isEmpty()) {
            final String site;
            final String url = getUrl();
            site = downloader.get(url, getExtractorLocalization()).responseBody();
            try {
                doc = JsonParser.object().from(site);
            } catch (JsonParserException jpe) {
                throw new ExtractionException("Could not parse json.", jpe);
            }
        }
        if (getLinkHandler().getContentFilters().contains(CONFERENCES)
                || getLinkHandler().getContentFilters().contains(ALL)
                || getLinkHandler().getContentFilters().isEmpty()) {
            conferenceKiosk.fetchPage();
        }
    }

    private void searchConferences(final String searchString,
                                   final List<ChannelInfoItem> channelItems,
                                   final InfoItemsSearchCollector collector) {
        for (final ChannelInfoItem item : channelItems) {
            if (item.getName().toUpperCase().contains(
                    searchString.toUpperCase())) {
                collector.commit(new ChannelInfoItemExtractor() {
                    @Override
                    public String getDescription() {
                        return item.getDescription();
                    }

                    @Override
                    public long getSubscriberCount() {
                        return item.getSubscriberCount();
                    }

                    @Override
                    public long getStreamCount() {
                        return item.getStreamCount();
                    }

                    @Override
                    public String getName() {
                        return item.getName();
                    }

                    @Override
                    public String getUrl() {
                        return item.getUrl();
                    }

                    @Override
                    public String getThumbnailUrl() {
                        return item.getThumbnailUrl();
                    }
                });
            }
        }
    }
}
