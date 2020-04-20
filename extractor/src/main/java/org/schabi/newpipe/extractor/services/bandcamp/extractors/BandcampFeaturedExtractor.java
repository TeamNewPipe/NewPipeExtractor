// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.InfoItemsCollector;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemsCollector;

import javax.annotation.Nonnull;
import java.io.IOException;

import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampChannelExtractor.getImageUrl;

public class BandcampFeaturedExtractor extends KioskExtractor<InfoItem> {

    public static final String KIOSK_FEATURED = "Featured";
    public static final String FEATURED_API_URL = "https://bandcamp.com/api/mobile/24/bootstrap_data";

    public BandcampFeaturedExtractor(StreamingService streamingService, ListLinkHandler listLinkHandler, String kioskId) {
        super(streamingService, listLinkHandler, kioskId);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {

    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return KIOSK_FEATURED;
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws IOException, ExtractionException {

        InfoItemsCollector c = new PlaylistInfoItemsCollector(getServiceId());

        try {


            JsonObject json = JsonParser.object().from(
                    getDownloader().post(
                            FEATURED_API_URL, null, "{\"platform\":\"\",\"version\":0}".getBytes()
                    ).responseBody()
            );

            JsonArray featuredStories = json.getObject("feed_content")
                    .getObject("stories")
                    .getArray("featured");

            for (int i = 0; i < featuredStories.size(); i++) {
                JsonObject featuredStory = featuredStories.getObject(i);

                if (featuredStory.isNull("album_title")) {
                    // Is not an album, ignore
                    continue;
                }

                c.commit(new BandcampPlaylistInfoItemExtractor(featuredStory));
            }

            return new InfoItemsPage<InfoItem>(c, null);
        } catch (JsonParserException e) {
            e.printStackTrace();
            throw new ParsingException("JSON error", e);
        }
    }

    @Override
    public String getNextPageUrl() {
        return null;
    }

    @Override
    public InfoItemsPage getPage(String pageUrl) {
        return null;
    }
}
