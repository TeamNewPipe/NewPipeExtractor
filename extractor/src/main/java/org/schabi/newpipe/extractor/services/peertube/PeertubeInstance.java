package org.schabi.newpipe.extractor.services.peertube;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.instance.AbstractInstance;
import org.schabi.newpipe.extractor.instance.InstanceMetaDataFetchException;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Utils;

public class PeertubeInstance extends AbstractInstance {

    public static final String SERVICE_NAME = "Peertube";

    public static final PeertubeInstance DEFAULT_INSTANCE =
            new PeertubeInstance("https://framatube.org", "FramaTube");

    public PeertubeInstance(final String url) {
        super(url, SERVICE_NAME);
    }

    public PeertubeInstance(final String url, final String name) {
        super(url, name);
    }

    @Override
    public void fetchMetadata() {
        final String urlToCheck = getUrl() + "/api/v1/config";
        try {
            final Response response = NewPipe.getDownloader().get(urlToCheck);

            if (response == null) {
                throw new IllegalStateException("Received no response");
            }
            if (Utils.isBlank(response.responseBody())) {
                throw new IllegalStateException("Received no response data");
            }

            final JsonObject json = JsonParser.object().from(response.responseBody());
            this.setName(JsonUtils.getString(json, "instance.name"));
        } catch (final Exception e) {
            throw new InstanceMetaDataFetchException(
                    "Unable to fetch config from " + urlToCheck, e);
        }
    }
}
