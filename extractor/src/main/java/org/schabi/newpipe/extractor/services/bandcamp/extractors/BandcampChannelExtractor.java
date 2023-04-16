// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import static org.schabi.newpipe.extractor.utils.Utils.replaceHttpWithHttps;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.jsoup.Jsoup;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.channel.ChannelTabExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.linkhandler.ChannelTabs;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.linkhandler.ReadyChannelTabListLinkHandler;
import org.schabi.newpipe.extractor.services.bandcamp.linkHandler.BandcampChannelTabLinkHandlerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

public class BandcampChannelExtractor extends ChannelExtractor {

    private JsonObject channelInfo;

    public BandcampChannelExtractor(final StreamingService service,
                                    final ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public String getAvatarUrl() {
        if (channelInfo.getLong("bio_image_id") == 0) {
            return "";
        }

        return BandcampExtractorHelper.getImageUrl(channelInfo.getLong("bio_image_id"), false);
    }

    @Override
    public String getBannerUrl() throws ParsingException {
        /*
         * Mobile API does not return the header or not the correct header.
         * Therefore, we need to query the website
         */
        try {
            final String html = getDownloader()
                    .get(replaceHttpWithHttps(channelInfo.getString("bandcamp_url")))
                    .responseBody();

            return Stream.of(Jsoup.parse(html).getElementById("customHeader"))
                    .filter(Objects::nonNull)
                    .flatMap(element -> element.getElementsByTag("img").stream())
                    .map(element -> element.attr("src"))
                    .findFirst()
                    .orElse(""); // no banner available

        } catch (final IOException | ReCaptchaException e) {
            throw new ParsingException("Could not download artist web site", e);
        }
    }

    /**
     * Bandcamp discontinued their RSS feeds because it hadn't been used enough.
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
    public List<ListLinkHandler> getTabs() throws ParsingException {
        final JsonArray discography = channelInfo.getArray("discography");
        final TabExtractorBuilder builder = new TabExtractorBuilder(discography);

        final List<ListLinkHandler> tabs = new ArrayList<>();

        if (discography.stream().anyMatch(o -> (
                (JsonObject) o).getString("item_type").equals("track"))) {
            tabs.add(new ReadyChannelTabListLinkHandler(getUrl(), getId(),
                    ChannelTabs.TRACKS, builder));
        }

        if (discography.stream().anyMatch(o -> (
                (JsonObject) o).getString("item_type").equals("album"))) {
            tabs.add(new ReadyChannelTabListLinkHandler(
                    getUrl() + BandcampChannelTabLinkHandlerFactory.URL_SUFFIX,
                    getId(), ChannelTabs.ALBUMS, builder));
        }

        return tabs;
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        channelInfo = BandcampExtractorHelper.getArtistDetails(getId());
    }

    @Nonnull
    @Override
    public String getName() {
        return channelInfo.getString("name");
    }

    private static class TabExtractorBuilder
            implements ReadyChannelTabListLinkHandler.ChannelTabExtractorBuilder {
        private final JsonArray discography;

        TabExtractorBuilder(final JsonArray discography) {
            this.discography = discography;
        }

        @Override
        public ChannelTabExtractor build(final StreamingService service,
                                         final ListLinkHandler linkHandler) {
            return BandcampChannelTabExtractor.fromDiscography(service, linkHandler, discography);
        }
    }
}
