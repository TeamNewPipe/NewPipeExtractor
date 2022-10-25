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
import org.schabi.newpipe.extractor.linkhandler.ChannelTabHandler;
import org.schabi.newpipe.extractor.services.bandcamp.linkHandler.BandcampChannelTabHandler;

import javax.annotation.Nonnull;
import java.io.IOException;

public class BandcampChannelTabExtractor extends ChannelTabExtractor {
    public BandcampChannelTabExtractor(final StreamingService service,
                                       final ChannelTabHandler linkHandler) {
        super(service, linkHandler);
    }

    @Nonnull
    private JsonArray getDiscographs() throws ExtractionException {
        final ChannelTabHandler tabHandler = getLinkHandler();
        if (tabHandler instanceof BandcampChannelTabHandler) {
            return ((BandcampChannelTabHandler) tabHandler).getDiscographs();
        } else {
            final JsonObject artistDetails = BandcampExtractorHelper.getArtistDetails(getId());
            return artistDetails.getArray("discography");
        }
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) {
        if (getLinkHandler().getTab() != ChannelTabHandler.Tab.Albums) {
            throw new IllegalArgumentException(
                    "tab " + getLinkHandler().getTab().name() + " not supported");
        }
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws IOException, ExtractionException {
        final MultiInfoItemsCollector collector = new MultiInfoItemsCollector(getServiceId());

        final JsonArray discography = getDiscographs();
        for (int i = 0; i < discography.size(); i++) {
            // A discograph is as an item appears in a discography
            final JsonObject discograph = discography.getObject(i);

            if (discograph.getString("item_type").equals("album")) {
                collector.commit(new BandcampAlbumInfoItemExtractor(discograph));
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
