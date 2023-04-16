package org.schabi.newpipe.extractor.services.media_ccc.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.MultiInfoItemsCollector;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.channel.ChannelTabExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ChannelTabs;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.linkhandler.ReadyChannelTabListLinkHandler;
import org.schabi.newpipe.extractor.services.media_ccc.extractors.infoItems.MediaCCCStreamInfoItemExtractor;
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCConferenceLinkHandlerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

public class MediaCCCConferenceExtractor extends ChannelExtractor {
    private JsonObject conferenceData;

    public MediaCCCConferenceExtractor(final StreamingService service,
                                       final ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public String getAvatarUrl() {
        return conferenceData.getString("logo_url");
    }

    @Override
    public String getBannerUrl() {
        return conferenceData.getString("logo_url");
    }

    @Override
    public String getFeedUrl() {
        return null;
    }

    @Override
    public long getSubscriberCount() {
        return -1;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getParentChannelName() {
        return "";
    }

    @Override
    public String getParentChannelUrl() {
        return "";
    }

    @Override
    public String getParentChannelAvatarUrl() {
        return "";
    }

    @Override
    public boolean isVerified() {
        return false;
    }

    @Nonnull
    @Override
    public List<ListLinkHandler> getTabs() throws ParsingException {
        return Collections.singletonList(new ReadyChannelTabListLinkHandler(getUrl(), getId(),
                ChannelTabs.VIDEOS, new VideoTabExtractorBuilder(conferenceData)));
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        final String conferenceUrl
                = MediaCCCConferenceLinkHandlerFactory.CONFERENCE_API_ENDPOINT + getId();
        try {
            conferenceData = JsonParser.object().from(downloader.get(conferenceUrl).responseBody());
        } catch (final JsonParserException jpe) {
            throw new ExtractionException("Could not parse json returnd by url: " + conferenceUrl);
        }
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return conferenceData.getString("title");
    }

    private static class VideoTabExtractorBuilder
            implements ReadyChannelTabListLinkHandler.ChannelTabExtractorBuilder {
        private final JsonObject conferenceData;

        VideoTabExtractorBuilder(final JsonObject conferenceData) {
            this.conferenceData = conferenceData;
        }

        @Override
        public ChannelTabExtractor build(final StreamingService service,
                                         final ListLinkHandler linkHandler) {
            return new VideoTabExtractor(service, linkHandler, conferenceData);
        }
    }

    private static class VideoTabExtractor extends ChannelTabExtractor {
        private final JsonObject conferenceData;

        VideoTabExtractor(final StreamingService service,
                                 final ListLinkHandler linkHandler,
                                 final JsonObject conferenceData) {
            super(service, linkHandler);
            this.conferenceData = conferenceData;
        }

        @Override
        public void onFetchPage(@Nonnull final Downloader downloader) {
            // nothing to do here, as data was already fetched
        }

        @Nonnull
        @Override
        public InfoItemsPage<InfoItem> getInitialPage() {
            final MultiInfoItemsCollector collector =
                    new MultiInfoItemsCollector(getServiceId());
            final JsonArray events = conferenceData.getArray("events");
            for (int i = 0; i < events.size(); i++) {
                collector.commit(new MediaCCCStreamInfoItemExtractor(events.getObject(i)));
            }
            return new InfoItemsPage<>(collector, null);
        }

        @Override
        public InfoItemsPage<InfoItem> getPage(final Page page) {
            return InfoItemsPage.emptyPage();
        }
    }
}
