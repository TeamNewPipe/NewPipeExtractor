package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.MultiInfoItemsCollector;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelTabExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ChannelTabs;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.streaminfoitem.BandcampDiscographStreamInfoItemExtractor;

import javax.annotation.Nonnull;
import java.io.IOException;

public class BandcampChannelTabExtractor extends ChannelTabExtractor {
    private JsonArray discography;
    private final String filter;

    public BandcampChannelTabExtractor(final StreamingService service,
                                       final ListLinkHandler linkHandler) {
        super(service, linkHandler);

        final String tab = linkHandler.getContentFilters().get(0);
        switch (tab) {
            case ChannelTabs.TRACKS:
                filter = "track";
                break;
            case ChannelTabs.ALBUMS:
                filter = "album";
                break;
            default:
                throw new IllegalArgumentException("unsupported channel tab: " + tab);
        }
    }

    public static BandcampChannelTabExtractor fromDiscography(final StreamingService service,
                                                              final ListLinkHandler linkHandler,
                                                              final JsonArray discography) {
        final BandcampChannelTabExtractor tabExtractor =
                new BandcampChannelTabExtractor(service, linkHandler);
        tabExtractor.discography = discography;
        return tabExtractor;
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) throws ParsingException {
        if (discography == null) {
            final JsonObject artistDetails = BandcampExtractorHelper.getArtistDetails(getId());
            discography = artistDetails.getArray("discography");
        }
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws IOException, ExtractionException {
        final MultiInfoItemsCollector collector = new MultiInfoItemsCollector(getServiceId());

        for (int i = 0; i < discography.size(); i++) {
            // A discograph is as an item appears in a discography
            final JsonObject discograph = discography.getObject(i);
            final String itemType = discograph.getString("item_type", "");

            if (!itemType.equals(filter)) {
                continue;
            }

            switch (itemType) {
                case "track":
                    collector.commit(new BandcampDiscographStreamInfoItemExtractor(
                            discograph, getUrl()));
                    break;
                case "album":
                    collector.commit(new BandcampAlbumInfoItemExtractor(
                            discograph, getUrl()));
                    break;
            }
        }

        return new InfoItemsPage<>(collector, null);
    }

    @Override
    public InfoItemsPage<InfoItem> getPage(final Page page) {
        return null;
    }
}
