package org.schabi.newpipe.extractor.services.soundcloud;

import org.schabi.newpipe.extractor.Downloader;
import org.schabi.newpipe.extractor.UrlIdHandler;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.kiosk.KioskExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemCollector;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

public class SoundcloudChartsExtractor extends KioskExtractor<SoundcloudService> {
	private String url;

    public SoundcloudChartsExtractor(SoundcloudService service, String url, String nextStreamsUrl, String kioskId)
            throws IOException, ExtractionException {
        super(service, url, nextStreamsUrl, kioskId);
        this.url = url;
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) {
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return "< Implement me (♥_♥) >";
    }

    @Nonnull
    @Override
    public UrlIdHandler getUrlIdHandler() {
        return new SoundcloudChartsUrlIdHandler();
    }

    @Override
    public NextItemsResult getNextStreams() throws IOException, ExtractionException {
        if (!hasMoreStreams()) {
            throw new ExtractionException("Chart doesn't have more streams");
        }

        StreamInfoItemCollector collector = new StreamInfoItemCollector(getServiceId());
        nextStreamsUrl = SoundcloudParsingHelper.getStreamsFromApi(collector, nextStreamsUrl, true);

        return new NextItemsResult(collector, nextStreamsUrl);
    }

    @Nonnull
    @Override
    public StreamInfoItemCollector getStreams() throws IOException, ExtractionException {
        StreamInfoItemCollector collector = new StreamInfoItemCollector(getServiceId());

        String apiUrl = "https://api-v2.soundcloud.com/charts" +
                "?genre=soundcloud:genres:all-music" +
                "&client_id=" + SoundcloudParsingHelper.clientId();

        if (getId().equals("Top 50")) {
            apiUrl += "&kind=top";
        } else {
            apiUrl += "&kind=trending";
        }

        List<String> supportedCountries = Arrays.asList("AU", "CA", "FR", "DE", "IE", "NL", "NZ", "GB", "US");
        String contentCountry = getContentCountry();
        if (supportedCountries.contains(contentCountry)) {
            apiUrl += "&region=soundcloud:regions:" + contentCountry;
        }

        nextStreamsUrl = SoundcloudParsingHelper.getStreamsFromApi(collector, apiUrl, true);
        return collector;
    }
}
