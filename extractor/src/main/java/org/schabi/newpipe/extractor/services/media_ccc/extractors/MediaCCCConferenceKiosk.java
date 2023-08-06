package org.schabi.newpipe.extractor.services.media_ccc.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelInfoItem;
import org.schabi.newpipe.extractor.channel.ChannelInfoItemsCollector;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.media_ccc.extractors.infoItems.MediaCCCConferenceInfoItemExtractor;

import java.io.IOException;

import javax.annotation.Nonnull;

public class MediaCCCConferenceKiosk extends KioskExtractor<ChannelInfoItem> {

    public static final String KIOSK_ID = "conferences";
    private JsonObject doc;

    public MediaCCCConferenceKiosk(final StreamingService streamingService,
                                   final ListLinkHandler linkHandler,
                                   final String kioskId) {
        super(streamingService, linkHandler, kioskId);
    }

    @Nonnull
    @Override
    public InfoItemsPage<ChannelInfoItem> getInitialPage() {
        final JsonArray conferences = doc.getArray("conferences");
        final ChannelInfoItemsCollector collector = new ChannelInfoItemsCollector(getServiceId());
        for (int i = 0; i < conferences.size(); i++) {
            collector.commit(new MediaCCCConferenceInfoItemExtractor(conferences.getObject(i)));
        }

        return new InfoItemsPage<>(collector, null);
    }

    @Override

    public InfoItemsPage<ChannelInfoItem> getPage(final Page page) {
        return InfoItemsPage.emptyPage();
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        final String site = downloader.get(getLinkHandler().getUrl(), getExtractorLocalization())
                .responseBody();
        try {
            doc = JsonParser.object().from(site);
        } catch (final JsonParserException jpe) {
            throw new ExtractionException("Could not parse json.", jpe);
        }
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return doc.getString("Conferences");
    }
}
