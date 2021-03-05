// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemsCollector;

import javax.annotation.Nonnull;
import java.io.IOException;

import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.BASE_API_URL;

public class BandcampFeaturedExtractor extends KioskExtractor<PlaylistInfoItem> {

    public static final String KIOSK_FEATURED = "Featured";
    public static final String FEATURED_API_URL = BASE_API_URL + "/mobile/24/bootstrap_data";

    private JsonObject json;

    public BandcampFeaturedExtractor(final StreamingService streamingService, final ListLinkHandler listLinkHandler,
                                     final String kioskId) {
        super(streamingService, listLinkHandler, kioskId);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        try {
            json = JsonParser.object().from(
                    getDownloader().post(
                            FEATURED_API_URL, null, "{\"platform\":\"\",\"version\":0}".getBytes()
                    ).responseBody()
            );
        } catch (final JsonParserException e) {
            throw new ParsingException("Could not parse Bandcamp featured API response", e);
        }
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return KIOSK_FEATURED;
    }

    @Nonnull
    @Override
    public InfoItemsPage<PlaylistInfoItem> getInitialPage() throws IOException, ExtractionException {

        final PlaylistInfoItemsCollector c = new PlaylistInfoItemsCollector(getServiceId());

        final JsonArray featuredStories = json.getObject("feed_content")
                .getObject("stories")
                .getArray("featured");

        for (int i = 0; i < featuredStories.size(); i++) {
            final JsonObject featuredStory = featuredStories.getObject(i);

            if (featuredStory.isNull("album_title")) {
                // Is not an album, ignore
                continue;
            }

            c.commit(new BandcampPlaylistInfoItemFeaturedExtractor(featuredStory));
        }

        return new InfoItemsPage<>(c, null);

    }

    @Override
    public InfoItemsPage<PlaylistInfoItem> getPage(Page page) {
        return null;
    }
}
