// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.InfoItemsCollector;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import javax.annotation.Nonnull;
import java.io.IOException;

public class BandcampRadioExtractor extends KioskExtractor<InfoItem> {

    public static final String KIOSK_RADIO = "Radio";
    public static final String RADIO_API_URL = "https://bandcamp.com/api/bcweekly/1/list";

    public BandcampRadioExtractor(StreamingService streamingService, ListLinkHandler linkHandler, String kioskId) {
        super(streamingService, linkHandler, kioskId);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {

    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return KIOSK_RADIO;
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws IOException, ExtractionException {
        InfoItemsCollector c = new StreamInfoItemsCollector(getServiceId());

        JSONObject json = new JSONObject(
                getDownloader().get(
                        RADIO_API_URL
                ).responseBody()
        );

        JSONArray radioShows = json.getJSONArray("results");

        for (int i = 0; i < radioShows.length(); i++) {
            JSONObject radioShow = radioShows.getJSONObject(i);

            c.commit(
                    new BandcampRadioInfoItemExtractor(radioShow)
            );
        }

        return new InfoItemsPage<InfoItem>(c, null);
    }

    @Override
    public String getNextPageUrl() throws IOException, ExtractionException {
        return null;
    }

    @Override
    public InfoItemsPage<InfoItem> getPage(String pageUrl) throws IOException, ExtractionException {
        return null;
    }
}
