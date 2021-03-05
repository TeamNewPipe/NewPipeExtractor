// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import org.jsoup.Jsoup;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.streaminfoitem.BandcampDiscographStreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import javax.annotation.Nonnull;
import java.io.IOException;

public class BandcampChannelExtractor extends ChannelExtractor {

    private JsonObject channelInfo;

    public BandcampChannelExtractor(final StreamingService service, final ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public String getAvatarUrl() {
        if (channelInfo.getLong("bio_image_id") == 0) return "";

        return BandcampExtractorHelper.getImageUrl(channelInfo.getLong("bio_image_id"), false);
    }

    @Override
    public String getBannerUrl() throws ParsingException {
        /*
         * Why does the mobile endpoint not contain the header?? Or at least not the same one?
         * Anyway we're back to querying websites
         */
        try {
            final String html = getDownloader()
                            .get(channelInfo.getString("bandcamp_url").replace("http://", "https://"))
                            .responseBody();

            return Jsoup.parse(html)
                    .getElementById("customHeader")
                    .getElementsByTag("img")
                    .first()
                    .attr("src");

        } catch (final IOException | ReCaptchaException e) {
            throw new ParsingException("Could not download artist web site", e);
        } catch (final NullPointerException e) {
            // No banner available
            return "";
        }
    }

    /**
     * bandcamp stopped providing RSS feeds when appending /feed to any URL
     * because too few people used it.
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

    @Override
    public String getParentChannelName() {
        return null;
    }

    @Override
    public String getParentChannelUrl() {
        return null;
    }

    @Override
    public String getParentChannelAvatarUrl() {
        return null;
    }

    @Override
    public boolean isVerified() throws ParsingException {
        return false;
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws ParsingException {

        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

        final JsonArray discography = channelInfo.getArray("discography");

        for (int i = 0; i < discography.size(); i++) {
            // I define discograph as an item that can appear in a discography
            final JsonObject discograph = discography.getObject(i);

            if (!discograph.getString("item_type").equals("track")) continue;

            collector.commit(new BandcampDiscographStreamInfoItemExtractor(discograph, getUrl()));
        }

        return new InfoItemsPage<>(collector, null);
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(Page page) {
        return null;
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        channelInfo = BandcampExtractorHelper.getArtistDetails(getId());
    }

    @Nonnull
    @Override
    public String getName() {
        return channelInfo.getString("name");
    }
}
