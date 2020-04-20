// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import com.grack.nanojson.*;
import org.jsoup.Jsoup;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import javax.annotation.Nonnull;
import java.io.IOException;

public class BandcampChannelExtractor extends ChannelExtractor {

    private JsonObject channelInfo;

    public BandcampChannelExtractor(StreamingService service, ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    /**
     * Fetch artist details from mobile endpoint.
     * <a href=https://notabug.org/fynngodau/bandcampDirect/wiki/rewindBandcamp+%E2%80%93+Fetching+artist+details>
     * I once took a moment to note down how it works.</a>
     */
    public static JsonObject getArtistDetails(String id) throws ParsingException {
        try {
            return
                    JsonParser.object().from(
                            NewPipe.getDownloader().post(
                                    "https://bandcamp.com/api/mobile/22/band_details",
                                    null,
                                    JsonWriter.string()
                                            .object()
                                            .value("band_id", id)
                                            .end()
                                            .done()
                                            .getBytes()
                            ).responseBody()
                    );
        } catch (IOException | ReCaptchaException | JsonParserException e) {
            throw new ParsingException("Could not download band details", e);
        }
    }

    /**
     * @param id    The image ID
     * @param album Whether this is the cover of an album
     * @return Url of image with this ID in size 10 which is 1200x1200 (we could also choose size 0
     * but we don't want something as large as 3460x3460 here, do we?)
     */
    public static String getImageUrl(long id, boolean album) {
        return "https://f4.bcbits.com/img/" + (album ? 'a' : "") + id + "_10.jpg";
    }

    @Override
    public String getAvatarUrl() {
        if (channelInfo.getLong("bio_image_id") == 0) return "";

        return getImageUrl(channelInfo.getLong("bio_image_id"), false);
    }

    /**
     * Why does the mobile endpoint not contain the header?? Or at least not the same one?
     * Anyway we're back to querying websites
     */
    @Override
    public String getBannerUrl() throws ParsingException {
        try {
            String html = getDownloader().get(channelInfo.getString("bandcamp_url").replace("http://", "https://"))
                    .responseBody();

            return Jsoup.parse(html)
                    .getElementById("customHeader")
                    .getElementsByTag("img")
                    .first()
                    .attr("src");

        } catch (IOException | ReCaptchaException e) {
            throw new ParsingException("Could not download artist web site", e);
        } catch (NullPointerException e) {
            // No banner available
            return "";
        }
    }

    /**
     * I had to learn bandcamp stopped providing RSS feeds when appending /feed to any URL
     * because too few people used it. Bummer!
     */
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
        return channelInfo.getString("bio");
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws ParsingException {

        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

        JsonArray discography = channelInfo.getArray("discography");

        for (int i = 0; i < discography.size(); i++) {
            // I define discograph as an item that can appear in a discography
            JsonObject discograph = discography.getObject(i);

            if (!discograph.getString("item_type").equals("track")) continue;

            collector.commit(new BandcampStreamInfoItemExtractor(
                    discograph.getString("title"),
                    BandcampExtractorHelper.getStreamUrlFromIds(
                            discograph.getLong("band_id"),
                            discograph.getLong("item_id"),
                            discograph.getString("item_type")
                    ),
                    getImageUrl(
                            discograph.getLong("art_id"), true
                    ),
                    discograph.getString("band_name")
            ));
        }

        return new InfoItemsPage<>(collector, null);
    }

    @Override
    public String getNextPageUrl() {
        return null;
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(String pageUrl) {
        return null;
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        channelInfo = getArtistDetails(getId());
    }

    @Nonnull
    @Override
    public String getName() {
        return channelInfo.getString("name");
    }
}
