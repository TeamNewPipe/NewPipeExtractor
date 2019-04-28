package org.schabi.newpipe.extractor.services.soundcloud;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import javax.annotation.Nonnull;
import java.io.IOException;

@SuppressWarnings("WeakerAccess")
public class SoundcloudChannelExtractor extends ChannelExtractor {
    private String userId;
    private JsonObject user;

    private StreamInfoItemsCollector streamInfoItemsCollector = null;
    private String nextPageUrl = null;

    public SoundcloudChannelExtractor(StreamingService service, ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {

        userId = getLinkHandler().getId();
        String apiUrl = "https://api-v2.soundcloud.com/users/" + userId +
                "?client_id=" + SoundcloudParsingHelper.clientId();

        String response = downloader.get(apiUrl, getExtractorLocalization()).responseBody();
        try {
            user = JsonParser.object().from(response);
        } catch (JsonParserException e) {
            throw new ParsingException("Could not parse json response", e);
        }
    }

    @Nonnull
    @Override
    public String getId() {
        return userId;
    }

    @Nonnull
    @Override
    public String getName() {
        return user.getString("username");
    }

    @Override
    public String getAvatarUrl() {
        return user.getString("avatar_url");
    }

    @Override
    public String getBannerUrl() {
        return user.getObject("visuals", new JsonObject())
                .getArray("visuals", new JsonArray())
                .getObject(0, new JsonObject())
                .getString("visual_url");
    }

    @Override
    public String getFeedUrl() {
        return null;
    }

    @Override
    public long getSubscriberCount() {
        return user.getNumber("followers_count", 0).longValue();
    }

    @Override
    public String getDescription() {
        return user.getString("description", "");
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws ExtractionException {
        if(streamInfoItemsCollector == null) {
            computeNextPageAndGetStreams();
        }
        return new InfoItemsPage<>(streamInfoItemsCollector, getNextPageUrl());
    }

    @Override
    public String getNextPageUrl() throws ExtractionException {
        if(nextPageUrl == null) {
            computeNextPageAndGetStreams();
        }
        return nextPageUrl;
    }

    private void computeNextPageAndGetStreams() throws ExtractionException {
        try {
            streamInfoItemsCollector = new StreamInfoItemsCollector(getServiceId());

            String apiUrl = "https://api-v2.soundcloud.com/users/" + getId() + "/tracks"
                    + "?client_id=" + SoundcloudParsingHelper.clientId()
                    + "&limit=20"
                    + "&linked_partitioning=1";

            nextPageUrl = SoundcloudParsingHelper.getStreamsFromApiMinItems(15, streamInfoItemsCollector, apiUrl);
        } catch (Exception e) {
            throw new ExtractionException("Could not get next page", e);
        }
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(final String pageUrl) throws IOException, ExtractionException {
        if (pageUrl == null || pageUrl.isEmpty()) {
            throw new ExtractionException(new IllegalArgumentException("Page url is empty or null"));
        }

        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        String nextPageUrl = SoundcloudParsingHelper.getStreamsFromApiMinItems(15, collector, pageUrl);

        return new InfoItemsPage<>(collector, nextPageUrl);
    }
}
