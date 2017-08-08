package org.schabi.newpipe.extractor.services.soundcloud;

import org.json.JSONObject;
import org.schabi.newpipe.extractor.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemCollector;

import java.io.IOException;

@SuppressWarnings("WeakerAccess")
public class SoundcloudPlaylistExtractor extends PlaylistExtractor {
    private String playlistId;
    private JSONObject playlist;

    public SoundcloudPlaylistExtractor(StreamingService service, String url, String nextStreamsUrl) throws IOException, ExtractionException {
        super(service, url, nextStreamsUrl);
    }

    @Override
    public void fetchPage() throws IOException, ExtractionException {
        Downloader dl = NewPipe.getDownloader();

        playlistId = getUrlIdHandler().getId(getOriginalUrl());
        String apiUrl = "https://api.soundcloud.com/playlists/" + playlistId +
                "?client_id=" + SoundcloudParsingHelper.clientId() +
                "&representation=compact";

        String response = dl.download(apiUrl);
        playlist = new JSONObject(response);
    }

    @Override
    public String getCleanUrl() {
        try {
            return playlist.getString("permalink_url");
        } catch (Exception e) {
            return getOriginalUrl();
        }
    }

    @Override
    public String getPlaylistId() {
        return playlistId;
    }

    @Override
    public String getPlaylistName() {
        return playlist.getString("title");
    }

    @Override
    public String getThumbnailUrl() {
        return playlist.optString("artwork_url");
    }

    @Override
    public String getBannerUrl() {
        return null;
    }

    @Override
    public String getUploaderUrl() {
        return playlist.getJSONObject("user").getString("permalink_url");
    }

    @Override
    public String getUploaderName() {
        return playlist.getJSONObject("user").getString("username");
    }

    @Override
    public String getUploaderAvatarUrl() {
        return playlist.getJSONObject("user").getString("avatar_url");
    }

    @Override
    public long getStreamCount() {
        return playlist.getLong("track_count");
    }

    @Override
    public StreamInfoItemCollector getStreams() throws IOException, ExtractionException {
        StreamInfoItemCollector collector = new StreamInfoItemCollector(getServiceId());

        // Note the "api", NOT "api-v2"
        String apiUrl = "https://api.soundcloud.com/playlists/" + getPlaylistId() + "/tracks"
                + "?client_id=" + SoundcloudParsingHelper.clientId()
                + "&limit=20"
                + "&linked_partitioning=1";

        nextStreamsUrl = SoundcloudParsingHelper.getStreamsFromApiMinItems(15, collector, apiUrl);
        return collector;
    }

    @Override
    public NextItemsResult getNextStreams() throws IOException, ExtractionException {
        if (!hasMoreStreams()) {
            throw new ExtractionException("Playlist doesn't have more streams");
        }

        StreamInfoItemCollector collector = new StreamInfoItemCollector(getServiceId());
        nextStreamsUrl = SoundcloudParsingHelper.getStreamsFromApiMinItems(15, collector, nextStreamsUrl);

        return new NextItemsResult(collector.getItemList(), nextStreamsUrl);
    }
}
