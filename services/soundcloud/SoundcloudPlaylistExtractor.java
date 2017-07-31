package org.schabi.newpipe.extractor.services.soundcloud;

import java.io.IOException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.schabi.newpipe.extractor.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.UrlIdHandler;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemCollector;

@SuppressWarnings("WeakerAccess")
public class SoundcloudPlaylistExtractor extends PlaylistExtractor {
    private String url;
    private String playlistId;
    private JSONObject playlist;
    private List<String> nextTracks;

    public SoundcloudPlaylistExtractor(UrlIdHandler urlIdHandler, String url, int serviceId) throws IOException, ExtractionException {
        super(urlIdHandler, urlIdHandler.cleanUrl(url), serviceId);

        Downloader dl = NewPipe.getDownloader();
        playlistId = urlIdHandler.getId(url);

        String apiUrl = "https://api-v2.soundcloud.com/users/" + playlistId
                + "?client_id=" + SoundcloudParsingHelper.clientId();

        String response = dl.download(apiUrl);
        playlist = new JSONObject(response);
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
    public String getAvatarUrl() {
        return playlist.getString("artwork_url");
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
    public long getStreamsCount() {
        return playlist.getLong("track_count");
    }

    @Override
    public StreamInfoItemCollector getStreams() throws ParsingException, ReCaptchaException, IOException {
        StreamInfoItemCollector collector = getStreamPreviewInfoCollector();
        Downloader dl = NewPipe.getDownloader();

        String apiUrl = "https://api-v2.soundcloud.com/playlists/" + playlistId
                + "?client_id=" + SoundcloudParsingHelper.clientId();

        String response = dl.download(apiUrl);
        JSONObject responseObject = new JSONObject(response);
        JSONArray responseCollection = responseObject.getJSONArray("collection");

        for (int i = 0; i < responseCollection.length(); i++) {
            JSONObject track = responseCollection.getJSONObject(i);
            try {
                collector.commit(new SoundcloudStreamInfoItemExtractor(track));
            } catch (Exception e) {
                nextTracks.add(track.getString("id"));
            }
        }
        return collector;
    }

    @Override
    public StreamInfoItemCollector getNextStreams() throws ReCaptchaException, IOException, ParsingException {
        if (nextTracks.equals(null)) {
            return null;
        }

        StreamInfoItemCollector collector = getStreamPreviewInfoCollector();
        Downloader dl = NewPipe.getDownloader();

        // TODO: Do this per 10 tracks, instead of all tracks at once
        String apiUrl = "https://api-v2.soundcloud.com/tracks?ids=";
        for (String id : nextTracks) {
            apiUrl += id;
            if (!id.equals(nextTracks.get(nextTracks.size() - 1))) {
                apiUrl += ",";
            }
        }
        apiUrl += "&client_id=" + SoundcloudParsingHelper.clientId();

        String response = dl.download(apiUrl);
        JSONObject responseObject = new JSONObject(response);
        JSONArray responseCollection = responseObject.getJSONArray("collection");

        for (int i = 0; i < responseCollection.length(); i++) {
            JSONObject track = responseCollection.getJSONObject(i);
            collector.commit(new SoundcloudStreamInfoItemExtractor(track));
        }
        nextTracks = null;
        return collector;
    }
}
