package org.schabi.newpipe.extractor.services.soundcloud;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.schabi.newpipe.extractor.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.UrlIdHandler;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.stream.StreamInfoItemCollector;

@SuppressWarnings("WeakerAccess")
public class SoundcloudChannelExtractor extends ChannelExtractor {
    private String url;
    private String channelId;
    private JSONObject channel;
    private String nextUrl;

    public SoundcloudChannelExtractor(UrlIdHandler urlIdHandler, String url, int serviceId) throws ExtractionException, IOException {
        super(urlIdHandler, url, serviceId);

        Downloader dl = NewPipe.getDownloader();

        channelId = urlIdHandler.getId(url);
        String apiUrl = "https://api-v2.soundcloud.com/users/" + channelId
                + "?client_id=" + SoundcloudParsingHelper.clientId();

        String response = dl.download(apiUrl);
        channel = new JSONObject(response);
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
    public StreamInfoItemCollector getStreams() throws ReCaptchaException, IOException, ParsingException {
        StreamInfoItemCollector collector = getStreamPreviewInfoCollector();
        Downloader dl = NewPipe.getDownloader();

        String apiUrl = "https://api-v2.soundcloud.com/users/" + channelId + "/tracks"
                + "?client_id=" + SoundcloudParsingHelper.clientId()
                + "&limit=10"
                + "&offset=0"
                + "&linked_partitioning=1";

        String response = dl.download(apiUrl);
        JSONObject responseObject = new JSONObject(response);

        nextUrl = responseObject.getString("next_href")
                + "&client_id=" + SoundcloudParsingHelper.clientId()
                + "&linked_partitioning=1";

        JSONArray responseCollection = responseObject.getJSONArray("collection");
        for (int i = 0; i < responseCollection.length(); i++) {
            JSONObject track = responseCollection.getJSONObject(i);
            collector.commit(new SoundcloudStreamInfoItemExtractor(track));
        }
        return collector;
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
    public StreamInfoItemCollector getNextStreams() throws ExtractionException, IOException {
        if (nextUrl.equals("")) {
            throw new ExtractionException("Channel doesn't have more streams");
        }

        StreamInfoItemCollector collector = getStreamPreviewInfoCollector();
        Downloader dl = NewPipe.getDownloader();

        String response = dl.download(nextUrl);
        JSONObject responseObject = new JSONObject(response);

        nextUrl = responseObject.getString("next_href")
                + "&client_id=" + SoundcloudParsingHelper.clientId()
                + "&linked_partitioning=1";

        JSONArray responseCollection = responseObject.getJSONArray("collection");
        for (int i = 0; i < responseCollection.length(); i++) {
            JSONObject track = responseCollection.getJSONObject(i);
            collector.commit(new SoundcloudStreamInfoItemExtractor(track));
        }
        return collector;
    }
}
