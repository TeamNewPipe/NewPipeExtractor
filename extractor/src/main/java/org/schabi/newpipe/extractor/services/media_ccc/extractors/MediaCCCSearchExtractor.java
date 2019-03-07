package org.schabi.newpipe.extractor.services.media_ccc.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.Downloader;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelInfoItem;
import org.schabi.newpipe.extractor.channel.ChannelInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler;
import org.schabi.newpipe.extractor.search.InfoItemsSearchCollector;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.media_ccc.extractors.infoItems.MediaCCCStreamInfoItemExtractor;
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCConferencesListLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Localization;
import static org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCSearchQueryHandlerFactory.CONFERENCES;
import static org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCSearchQueryHandlerFactory.EVENTS;
import static org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCSearchQueryHandlerFactory.ALL;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

public class MediaCCCSearchExtractor extends SearchExtractor {

    private JsonObject doc;
    private MediaCCCConferenceKiosk conferenceKiosk;

    public MediaCCCSearchExtractor(StreamingService service, SearchQueryHandler linkHandler, Localization localization) {
        super(service, linkHandler, localization);
        try {
            conferenceKiosk = new MediaCCCConferenceKiosk(service,
                    new MediaCCCConferencesListLinkHandlerFactory().fromId("conferences"),
                    "conferences",
                    localization);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getSearchSuggestion() throws ParsingException {
        return null;
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws IOException, ExtractionException {
        InfoItemsSearchCollector searchItems = getInfoItemSearchCollector();

        if(getLinkHandler().getContentFilters().contains(CONFERENCES)
                || getLinkHandler().getContentFilters().contains(ALL)
                || getLinkHandler().getContentFilters().isEmpty()) {
            searchConferences(getSearchString(),
                    conferenceKiosk.getInitialPage().getItems(),
                    searchItems);
        }

        if(getLinkHandler().getContentFilters().contains(EVENTS)
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
    public String getNextPageUrl() throws IOException, ExtractionException {
        return "";
    }

    @Override
    public InfoItemsPage<InfoItem> getPage(String pageUrl) throws IOException, ExtractionException {
        return InfoItemsPage.emptyPage();
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        if(getLinkHandler().getContentFilters().contains(EVENTS)
            || getLinkHandler().getContentFilters().contains(ALL)
                || getLinkHandler().getContentFilters().isEmpty()) {
            final String site;
            final String url = getUrl();
            site = downloader.download(url, getLocalization());
            try {
                doc = JsonParser.object().from(site);
            } catch (JsonParserException jpe) {
                throw new ExtractionException("Could not parse json.", jpe);
            }
        }
        if(getLinkHandler().getContentFilters().contains(CONFERENCES)
                || getLinkHandler().getContentFilters().contains(ALL)
                || getLinkHandler().getContentFilters().isEmpty())
        conferenceKiosk.fetchPage();
    }

    private void searchConferences(String searchString,
                                                    List<ChannelInfoItem> channelItems,
                                                    InfoItemsSearchCollector collector) {
        for(final ChannelInfoItem item : channelItems) {
            if(item.getName().toUpperCase().contains(
                    searchString.toUpperCase())) {
                collector.commit(new ChannelInfoItemExtractor() {
                    @Override
                    public String getDescription() throws ParsingException {
                        return item.getDescription();
                    }

                    @Override
                    public long getSubscriberCount() throws ParsingException {
                        return item.getSubscriberCount();
                    }

                    @Override
                    public long getStreamCount() throws ParsingException {
                        return item.getStreamCount();
                    }

                    @Override
                    public String getName() throws ParsingException {
                        return item.getName();
                    }

                    @Override
                    public String getUrl() throws ParsingException {
                        return item.getUrl();
                    }

                    @Override
                    public String getThumbnailUrl() throws ParsingException {
                        return item.getThumbnailUrl();
                    }
                });
            }
        }
    }
}
