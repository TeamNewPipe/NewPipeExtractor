package org.schabi.newpipe.extractor.services.media_ccc.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import javax.annotation.Nonnull;
import java.io.IOException;

public class MediaCCCLiveStreamKiosk extends KioskExtractor<StreamInfoItem> {

    public static final String KIOSK_ID = "live";
    private JsonArray doc;

    public MediaCCCLiveStreamKiosk(final StreamingService streamingService,
                                   final ListLinkHandler linkHandler,
                                   final String kioskId) {
        super(streamingService, linkHandler, kioskId);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        doc = MediaCCCParsingHelper.getLiveStreams(downloader, getExtractorLocalization());
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws IOException, ExtractionException {
        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        for (int c = 0; c < doc.size(); c++) {
            final JsonObject conference = doc.getObject(c);
            if (conference.getBoolean("isCurrentlyStreaming")) {
                final JsonArray groups = conference.getArray("groups");
                for (int g = 0; g < groups.size(); g++) {
                    final String group = groups.getObject(g).getString("group");
                    final JsonArray rooms = groups.getObject(g).getArray("rooms");
                    for (int r = 0; r < rooms.size(); r++) {
                        final JsonObject room = rooms.getObject(r);
                        collector.commit(new MediaCCCLiveStreamKioskExtractor(
                                conference, group, room));
                    }
                }
            }

        }
        return new InfoItemsPage<>(collector, null);
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(final Page page)
            throws IOException, ExtractionException {
        return InfoItemsPage.emptyPage();
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return KIOSK_ID;
    }
}
