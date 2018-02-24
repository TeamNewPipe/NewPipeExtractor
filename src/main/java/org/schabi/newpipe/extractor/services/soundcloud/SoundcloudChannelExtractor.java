package org.schabi.newpipe.extractor.services.soundcloud;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.Downloader;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import javax.annotation.Nonnull;
import java.io.IOException;

@SuppressWarnings("WeakerAccess")
public class SoundcloudChannelExtractor extends ChannelExtractor {
    private String userId;
    private JsonObject user;

    public SoundcloudChannelExtractor(StreamingService service, String url, String nextPageUrl) throws IOException, ExtractionException {
        super(service, url, nextPageUrl);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {

        userId = getUrlIdHandler().getId(getOriginalUrl());
        String apiUrl = "https://api.soundcloud.com/users/" + userId +
                "?client_id=" + SoundcloudParsingHelper.clientId();

        String response = downloader.download(apiUrl);
        try {
            user = JsonParser.object().from(response);
        } catch (JsonParserException e) {
            throw new ParsingException("Could not parse json response", e);
        }
    }

    @Nonnull
    @Override
    public String getCleanUrl() {
        return user.isString("permalink_url") ? user.getString("permalink_url") : getOriginalUrl();
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
        try {
            return user.getObject("visuals").getArray("visuals").getObject(0).getString("visual_url", "");
        } catch (NullPointerException e) {
            return null;
        }
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
    public String getDescription() throws ParsingException {
        return user.getString("description", "");
    }

    @Nonnull
    @Override
    public StreamInfoItemsCollector getStreams() throws IOException, ExtractionException {
        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

        String apiUrl = "https://api-v2.soundcloud.com/users/" + getId() + "/tracks"
                + "?client_id=" + SoundcloudParsingHelper.clientId()
                + "&limit=20"
                + "&linked_partitioning=1";

        nextPageUrl = SoundcloudParsingHelper.getStreamsFromApiMinItems(15, collector, apiUrl);
        return collector;
    }

    @Override
    public InfoItemPage getInfoItemPage() throws IOException, ExtractionException {
        if (!hasNextPage()) {
            throw new ExtractionException("Channel doesn't have more streams");
        }

        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        nextPageUrl = SoundcloudParsingHelper.getStreamsFromApiMinItems(15, collector, nextPageUrl);

        return new InfoItemPage(collector, nextPageUrl);
    }
}
