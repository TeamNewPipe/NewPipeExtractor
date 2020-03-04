package org.schabi.newpipe.extractor.services.media_ccc.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelTabExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.MixedInfoItemsCollector;
import org.schabi.newpipe.extractor.services.media_ccc.extractors.infoItems.MediaCCCStreamInfoItemExtractor;

import java.io.IOException;

import javax.annotation.Nonnull;

public class MediaCCCConferenceEventsExtractor extends ChannelTabExtractor {
    private JsonObject conferenceData;

    public MediaCCCConferenceEventsExtractor(StreamingService service, ListLinkHandler linkHandler, JsonObject conferenceData) {
        super(service, linkHandler);

        this.conferenceData = conferenceData;
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws IOException, ExtractionException {
        MixedInfoItemsCollector collector = new MixedInfoItemsCollector(getServiceId());
        JsonArray events = conferenceData.getArray("events");
        for (int i = 0; i < events.size(); i++) {
            collector.commit(new MediaCCCStreamInfoItemExtractor(events.getObject(i)));
        }
        return new InfoItemsPage<>(collector, null);
    }

    @Override
    public String getNextPageUrl() throws IOException, ExtractionException {
        return null;
    }

    @Override
    public InfoItemsPage<InfoItem> getPage(String pageUrl) throws IOException, ExtractionException {
        return null;
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {}

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return "Events";
    }
}
