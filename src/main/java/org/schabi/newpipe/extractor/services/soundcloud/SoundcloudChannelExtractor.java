package org.schabi.newpipe.extractor.services.soundcloud;

import org.json.JSONObject;
import org.schabi.newpipe.extractor.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.stream.StreamInfoItemCollector;

import java.io.IOException;

@SuppressWarnings("WeakerAccess")
public class SoundcloudChannelExtractor extends ChannelExtractor {
    private String channelId;
    private JSONObject channel;

    public SoundcloudChannelExtractor(StreamingService service, String url, String nextStreamsUrl) throws IOException, ExtractionException {
        super(service, url, nextStreamsUrl);
    }

    @Override
    public void fetchPage() throws IOException, ExtractionException {
        Downloader dl = NewPipe.getDownloader();

        channelId = getUrlIdHandler().getId(getOriginalUrl());
        String apiUrl = "https://api.soundcloud.com/users/" + channelId +
                "?client_id=" + SoundcloudParsingHelper.clientId();

        String response = dl.download(apiUrl);
        channel = new JSONObject(response);
    }

    @Override
    public String getCleanUrl() {
        try {
            return channel.getString("permalink_url");
        } catch (Exception e) {
            return getOriginalUrl();
        }
    }

    @Override
    public String getChannelId() {
        return channelId;
    }

    @Override
    public String getChannelName() {
        return channel.getString("username");
    }

    @Override
    public String getAvatarUrl() {
        return channel.getString("avatar_url");
    }

    @Override
    public String getBannerUrl() throws ParsingException {
        try {
            return channel.getJSONObject("visuals").getJSONArray("visuals").getJSONObject(0).getString("visual_url");
        } catch (Exception e) {
            throw new ParsingException("Could not get Banner", e);
        }
    }

    @Override
    public long getSubscriberCount() {
        return channel.getLong("followers_count");
    }

    @Override
    public String getFeedUrl() throws ParsingException {
        return null;
    }

    @Override
    public StreamInfoItemCollector getStreams() throws IOException, ExtractionException {
        StreamInfoItemCollector collector = new StreamInfoItemCollector(getServiceId());

        String apiUrl = "https://api-v2.soundcloud.com/users/" + getChannelId() + "/tracks"
                + "?client_id=" + SoundcloudParsingHelper.clientId()
                + "&limit=20"
                + "&linked_partitioning=1";

        nextStreamsUrl = SoundcloudParsingHelper.getStreamsFromApiMinItems(15, collector, apiUrl);
        return collector;
    }

    @Override
    public NextItemsResult getNextStreams() throws IOException, ExtractionException {
        if (!hasMoreStreams()) {
            throw new ExtractionException("Channel doesn't have more streams");
        }

        StreamInfoItemCollector collector = new StreamInfoItemCollector(getServiceId());
        nextStreamsUrl = SoundcloudParsingHelper.getStreamsFromApiMinItems(15, collector, nextStreamsUrl);

        return new NextItemsResult(collector.getItemList(), nextStreamsUrl);
    }
}
