// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.schabi.newpipe.extractor.Collector;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.InfoItemsCollector;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemsCollector;
import org.schabi.newpipe.extractor.search.InfoItemsSearchCollector;
import org.schabi.newpipe.extractor.services.bandcamp.linkHandler.BandcampFeaturedLinkHandlerFactory;

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

        JSONObject json = new JSONObject(
                getDownloader().post(
                        FEATURED_API_URL, null, "{\"platform\":\"\",\"version\":0}".getBytes()
                ).responseBody()
        );

        JSONArray featuredStories = json.getJSONObject("feed_content")
                .getJSONObject("stories")
                .getJSONArray("featured");

        for (int i = 0; i < featuredStories.length(); i++) {
            JSONObject featuredStory = featuredStories.getJSONObject(i);

            if (featuredStory.isNull("album_title")) {
                // Is not an album, ignore
                continue;
            }

            c.commit(new BandcampPlaylistInfoItemExtractor(
                    featuredStory.getString("album_title"),
                    featuredStory.getString("band_name"),
                    featuredStory.getString("item_url"),
                    featuredStory.has("art_id") ? getImageUrl(featuredStory.getLong("art_id"), true) : "",
                    featuredStory.getInt("num_streamable_tracks")
            ));
        }

        return new InfoItemsPage<InfoItem>(c, null);
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
