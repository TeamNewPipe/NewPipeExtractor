// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import static org.schabi.newpipe.extractor.utils.Utils.replaceHttpWithHttps;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.jsoup.Jsoup;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.MultiInfoItemsCollector;
import org.schabi.newpipe.extractor.Page;
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
import org.schabi.newpipe.extractor.services.bandcamp.extractors.streaminfoitem.BandcampDiscographStreamInfoItemExtractor;
import org.schabi.newpipe.extractor.services.bandcamp.linkHandler.BandcampChannelTabHandler;
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

        final List<ListLinkHandler> tabs = new ArrayList<>();
        tabs.add(new ReadyChannelTabListLinkHandler(getUrl(), getId(),
                ChannelTabs.TRACKS, this::buildTracksTabExtractor));

        if (discography.stream().anyMatch(o -> (
                (JsonObject) o).getString("item_type").equals("album"))) {
            tabs.add(new BandcampChannelTabHandler(
                    getUrl() + BandcampChannelTabLinkHandlerFactory.URL_SUFFIX,
                    getId(), ChannelTabs.ALBUMS, discography));
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

    private ChannelTabExtractor buildTracksTabExtractor(final StreamingService service,
                                                        final ListLinkHandler linkHandler) {
        return new ChannelTabExtractor(service, linkHandler) {
            @Nonnull
            @Override
            public InfoItemsPage<InfoItem> getInitialPage() throws ExtractionException {
                final MultiInfoItemsCollector collector =
                        new MultiInfoItemsCollector(getServiceId());

                final JsonArray discography = channelInfo.getArray("discography");

                for (int i = 0; i < discography.size(); i++) {
                    // A discograph is as an item appears in a discography
                    final JsonObject discograph = discography.getObject(i);

                    if (!discograph.getString("item_type").equals("track")) {
                        continue;
                    }

                    collector.commit(
                            new BandcampDiscographStreamInfoItemExtractor(discograph, getUrl()));
                }

                return new InfoItemsPage<>(collector, null);
            }

            @Override
            public InfoItemsPage<InfoItem> getPage(final Page page) {
                return ListExtractor.InfoItemsPage.emptyPage();
            }

            @Override
            public void onFetchPage(@Nonnull final Downloader downloader) {
                // nothing to do here, as data was already fetched
            }
        };
    }
}
