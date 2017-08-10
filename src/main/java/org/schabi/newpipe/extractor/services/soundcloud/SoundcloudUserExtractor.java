package org.schabi.newpipe.extractor.services.soundcloud;

import org.json.JSONObject;
import org.schabi.newpipe.extractor.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.stream.StreamInfoItemCollector;
import org.schabi.newpipe.extractor.user.UserExtractor;

import java.io.IOException;

@SuppressWarnings("WeakerAccess")
public class SoundcloudUserExtractor extends UserExtractor {
    private String userId;
    private JSONObject user;

    public SoundcloudUserExtractor(StreamingService service, String url, String nextStreamsUrl) throws IOException, ExtractionException {
        super(service, url, nextStreamsUrl);
    }

    @Override
    public void fetchPage() throws IOException, ExtractionException {
        Downloader dl = NewPipe.getDownloader();

        userId = getUrlIdHandler().getId(getOriginalUrl());
        String apiUrl = "https://api.soundcloud.com/users/" + userId +
                "?client_id=" + SoundcloudParsingHelper.clientId();

        String response = dl.download(apiUrl);
        user = new JSONObject(response);
    }

    @Override
    public String getCleanUrl() {
        try {
            return user.getString("permalink_url");
        } catch (Exception e) {
            return getOriginalUrl();
        }
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public String getUserName() {
        return user.getString("username");
    }

    @Override
    public String getAvatarUrl() {
        return user.optString("avatar_url");
    }

    @Override
    public String getBannerUrl() throws ParsingException {
        try {
            return user.getJSONObject("visuals").getJSONArray("visuals").getJSONObject(0).getString("visual_url");
        } catch (Exception e) {
            throw new ParsingException("Could not get Banner", e);
        }
    }

    @Override
    public long getSubscriberCount() {
        return user.optLong("followers_count", 0L);
    }

    @Override
    public String getFeedUrl() throws ParsingException {
        return null;
    }

    @Override
    public StreamInfoItemCollector getStreams() throws IOException, ExtractionException {
        StreamInfoItemCollector collector = new StreamInfoItemCollector(getServiceId());

        String apiUrl = "https://api-v2.soundcloud.com/users/" + getUserId() + "/tracks"
                + "?client_id=" + SoundcloudParsingHelper.clientId()
                + "&limit=20"
                + "&linked_partitioning=1";

        nextStreamsUrl = SoundcloudParsingHelper.getStreamsFromApiMinItems(15, collector, apiUrl);
        return collector;
    }

    @Override
    public NextItemsResult getNextStreams() throws IOException, ExtractionException {
        if (!hasMoreStreams()) {
            throw new ExtractionException("User doesn't have more streams");
        }

        StreamInfoItemCollector collector = new StreamInfoItemCollector(getServiceId());
        nextStreamsUrl = SoundcloudParsingHelper.getStreamsFromApiMinItems(15, collector, nextStreamsUrl);

        return new NextItemsResult(collector.getItemList(), nextStreamsUrl);
    }

    @Override
    public String getDescription() throws ParsingException {
        return user.optString("description");
    }
}
