// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.InfoItemsCollector;
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

        try {

            JsonObject json = JsonParser.object().from(
                    getDownloader().get(
                            RADIO_API_URL
                    ).responseBody()
            );

            JsonArray radioShows = json.getArray("results");

            for (int i = 0; i < radioShows.size(); i++) {
                JsonObject radioShow = radioShows.getObject(i);

                c.commit(
                        new BandcampRadioInfoItemExtractor(radioShow)
                );
            }

        } catch (JsonParserException e) {
            e.printStackTrace();
        }

        return new InfoItemsPage<InfoItem>(c, null);
    }

    @Override
    public InfoItemsPage<InfoItem> getPage(Page page) {
        return null;
    }
}
