// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

    private JSONObject channelInfo;

    public BandcampChannelExtractor(StreamingService service, ListLinkHandler linkHandler) throws ParsingException {
        super(service, linkHandler);

        channelInfo = getArtistDetails(getId());
    }

    /**
     * Fetch artist details from mobile endpoint.
     * <a href=https://notabug.org/fynngodau/bandcampDirect/wiki/rewindBandcamp+%E2%80%93+Fetching+artist+details>
     * I once took a moment to note down how it works.</a>
     */
    public static JSONObject getArtistDetails(String id) throws ParsingException {
        try {
            return
                    new JSONObject(
                            NewPipe.getDownloader().post(
                                    "https://bandcamp.com/api/mobile/22/band_details",
                                    null,
                                    ("{\"band_id\":\"" + id + "\"}").getBytes()
                            ).responseBody()
                    );
        } catch (IOException | ReCaptchaException e) {
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
        try {
            return getImageUrl(channelInfo.getLong("bio_image_id"), false);
        } catch (JSONException e) {
            // In this case, the id is null and no image is available
            return "";
        }
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

        JSONArray discography = channelInfo.getJSONArray("discography");

        for (int i = 0; i < discography.length(); i++) {
            // I define discograph as an item that can appear in a discography
            JSONObject discograph = discography.getJSONObject(i);

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
    }

    @Nonnull
    @Override
    public String getName() {
        return channelInfo.getString("name");
    }
}
