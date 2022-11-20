package org.schabi.newpipe.extractor.services.soundcloud.extractors;

import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;
import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import java.io.IOException;

import javax.annotation.Nonnull;

public class SoundcloudChartsExtractor extends KioskExtractor<StreamInfoItem> {
    public SoundcloudChartsExtractor(final StreamingService service,
                                     final ListLinkHandler linkHandler,
                                     final String kioskId) {
        super(service, linkHandler, kioskId);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) {
    }

    @Nonnull
    @Override
    public String getName() {
        return getId();
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(final Page page) throws IOException,
            ExtractionException {
        if (page == null || isNullOrEmpty(page.getUrl())) {
            throw new IllegalArgumentException("Page doesn't contain an URL");
        }

        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        final String nextPageUrl = SoundcloudParsingHelper.getStreamsFromApi(collector,
                page.getUrl(), true);

        return new InfoItemsPage<>(collector, new Page(nextPageUrl));
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws IOException, ExtractionException {
        final var collector = new StreamInfoItemsCollector(getServiceId());
        final var apiUrlBuilder = SOUNDCLOUD_API_V2_URL.newBuilder()
                .addPathSegment("charts")
                .addQueryParameter("genre", "soundcloud:genres:all-music")
                .addQueryParameter("client_id", SoundcloudParsingHelper.clientId())
                .addQueryParameter("kind", getId().equals("Top 50") ? "top" : "trending");

        final var contentCountry = SoundCloud.getContentCountry();
        if (getService().getSupportedCountries().contains(contentCountry)) {
            apiUrlBuilder.addQueryParameter("region",
                    "soundcloud:regions:" + contentCountry.getCountryCode());
        }

        String nextPageUrl;
        try {
            nextPageUrl = SoundcloudParsingHelper.getStreamsFromApi(collector,
                    apiUrlBuilder.toString(), true);
        } catch (final IOException e) {
            // Request to other region may be geo-restricted.
            // See https://github.com/TeamNewPipe/NewPipeExtractor/issues/537.
            // We retry without the specified region.
            final var apiUrlWithoutRegion = apiUrlBuilder
                    .removeAllQueryParameters("region")
                    .toString();
            nextPageUrl = SoundcloudParsingHelper.getStreamsFromApi(collector, apiUrlWithoutRegion,
                    true);
        }

        return new InfoItemsPage<>(collector, new Page(nextPageUrl));
    }
}
