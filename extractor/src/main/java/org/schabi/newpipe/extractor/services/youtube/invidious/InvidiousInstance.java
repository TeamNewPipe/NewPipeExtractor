package org.schabi.newpipe.extractor.services.youtube.invidious;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.instance.InstanceMetaDataFetchException;
import org.schabi.newpipe.extractor.services.youtube.YoutubeLikeInstance;
import org.schabi.newpipe.extractor.utils.Utils;

public class InvidiousInstance extends YoutubeLikeInstance<InvidiousService> {

    public InvidiousInstance(final String url) {
        super(url, tryExtractDomainFromUrl(url, "Invidious instance" + url.hashCode()));
    }

    public InvidiousInstance(final String url, final String name) {
        super(url, name);
    }

    @Override
    public void fetchMetadata() {
        checkStatsEndpoint();
        checkTrendingEndpoint();
    }

    private void checkStatsEndpoint() {
        checkEndpoint("/api/v1/stats?fields=software,error", r -> {
            final JsonObject json = JsonParser.object().from(r.responseBody());
            if (!json.has("software")
                    && !"Statistics are not enabled.".equals(json.getString("error"))) {
                throw new IllegalStateException("Could not get stats");
            }
        });
    }

    private void checkTrendingEndpoint() {
        checkEndpoint("/api/v1/trending", r -> {
            final JsonArray json = JsonParser.array().from(r.responseBody());
            if (json.isEmpty()) {
                throw new IllegalStateException("No data returned");
            }
        });
    }

    private void checkEndpoint(
            final String subUrlToCheck,
            final ResponseCheck responseCheck
    ) {
        final String urlToCheck = getUrl() + subUrlToCheck;
        try {
            final Response response = NewPipe.getDownloader().get(urlToCheck);

            if (response == null) {
                throw new IllegalStateException("Received no response");
            }
            if (Utils.isBlank(response.responseBody())) {
                throw new IllegalStateException("Received no response data");
            }

            responseCheck.check(response);
        } catch (final Exception e) {
            throw new InstanceMetaDataFetchException(
                    "Unable to fetch trending from " + urlToCheck, e);
        }
    }

    @Override
    public InvidiousService getNewStreamingService(final int id) {
        return new InvidiousService(id, this);
    }

    @Override
    public String getServiceName() {
        return "Invidious";
    }

    @FunctionalInterface
    public interface ResponseCheck {
        void check(final Response r) throws Exception;
    }
}
