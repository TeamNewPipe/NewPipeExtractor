package org.schabi.newpipe.extractor.services.soundcloud;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class SoundcloudPlaylistExtractor extends PlaylistExtractor {
    private static final int streamsPerRequestedPage = 15;

    private String playlistId;
    private JsonObject playlist;

    private StreamInfoItemsCollector streamInfoItemsCollector;
    private List<Integer> nextTrackIds;
    private int nextTrackIdsIndex;
    private String nextPageUrl;

    public SoundcloudPlaylistExtractor(StreamingService service, ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {

        playlistId = getLinkHandler().getId();
        String apiUrl = "https://api-v2.soundcloud.com/playlists/" + playlistId +
                "?client_id=" + SoundcloudParsingHelper.clientId() +
                "&representation=compact";

        String response = downloader.get(apiUrl, getExtractorLocalization()).responseBody();
        try {
            playlist = JsonParser.object().from(response);
        } catch (JsonParserException e) {
            throw new ParsingException("Could not parse json response", e);
        }
    }

    @Nonnull
    @Override
    public String getId() {
        return playlistId;
    }

    @Nonnull
    @Override
    public String getName() {
        return playlist.getString("title");
    }

    @Override
    public String getThumbnailUrl() {
        String artworkUrl = playlist.getString("artwork_url");

        if (artworkUrl == null) {
            // If the thumbnail is null, traverse the items list and get a valid one,
            // if it also fails, return null
            try {
                final InfoItemsPage<StreamInfoItem> infoItems = getInitialPage();
                if (infoItems.getItems().isEmpty()) return null;

                for (StreamInfoItem item : infoItems.getItems()) {
                    final String thumbnailUrl = item.getThumbnailUrl();
                    if (thumbnailUrl == null || thumbnailUrl.isEmpty()) continue;

                    String thumbnailUrlBetterResolution = thumbnailUrl.replace("large.jpg", "crop.jpg");
                    return thumbnailUrlBetterResolution;
                }
            } catch (Exception ignored) {
            }
        }

        String artworkUrlBetterResolution = artworkUrl.replace("large.jpg", "crop.jpg");
        return artworkUrlBetterResolution;
    }

    @Override
    public String getBannerUrl() {
        return null;
    }

    @Override
    public String getUploaderUrl() {
        return SoundcloudParsingHelper.getUploaderUrl(playlist);
    }

    @Override
    public String getUploaderName() {
        return SoundcloudParsingHelper.getUploaderName(playlist);
    }

    @Override
    public String getUploaderAvatarUrl() {
        return SoundcloudParsingHelper.getAvatarUrl(playlist);
    }

    @Override
    public long getStreamCount() {
        return playlist.getNumber("track_count", 0).longValue();
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws IOException, ExtractionException {
        if (streamInfoItemsCollector == null) {
            computeInitialTracksAndNextIds();
        }
        return new InfoItemsPage<>(streamInfoItemsCollector, getNextPageUrl());
    }

    private void computeInitialTracksAndNextIds() {
        streamInfoItemsCollector = new StreamInfoItemsCollector(getServiceId());
        nextTrackIds = new ArrayList<>();
        nextTrackIdsIndex = 0;

        JsonArray tracks = playlist.getArray("tracks");
        for (Object o : tracks) {
            if (o instanceof JsonObject) {
                JsonObject track = (JsonObject) o;
                if (track.has("title")) { // i.e. if full info is available
                    streamInfoItemsCollector.commit(new SoundcloudStreamInfoItemExtractor(track));
                } else {
                    nextTrackIds.add(track.getInt("id"));
                }
            }
        }
    }

    private void computeAnotherNextPageUrl() throws IOException, ExtractionException {
        if (nextTrackIdsIndex >= nextTrackIds.size()) {
            nextPageUrl = ""; // there are no more tracks
            return;
        }

        StringBuilder urlBuilder = new StringBuilder("https://api-v2.soundcloud.com/tracks?client_id=");
        urlBuilder.append(SoundcloudParsingHelper.clientId());
        urlBuilder.append("&ids=");

        int upperIndex = Math.min(nextTrackIdsIndex + streamsPerRequestedPage, nextTrackIds.size());
        for (int i = nextTrackIdsIndex; i < upperIndex; ++i) {
            urlBuilder.append(nextTrackIds.get(i));
            urlBuilder.append(","); // a , at the end is ok
        }

        nextPageUrl = urlBuilder.toString();
    }

    @Override
    public String getNextPageUrl() throws IOException, ExtractionException {
        if (nextPageUrl == null) {
            if (nextTrackIds == null) {
                computeInitialTracksAndNextIds();
            }
            computeAnotherNextPageUrl();
        }
        return nextPageUrl;
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(String pageUrl) throws IOException, ExtractionException {
        if (pageUrl == null || pageUrl.isEmpty()) {
            throw new ExtractionException(new IllegalArgumentException("Page url is empty or null"));
        }

        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        String response = NewPipe.getDownloader().get(pageUrl, getExtractorLocalization()).responseBody();

        try {
            JsonArray tracks = JsonParser.array().from(response);
            for (Object track : tracks) {
                if (track instanceof JsonObject) {
                    collector.commit(new SoundcloudStreamInfoItemExtractor((JsonObject) track));
                }
            }
        } catch (JsonParserException e) {
            throw new ParsingException("Could not parse json response", e);
        }

        computeAnotherNextPageUrl();
        return new InfoItemsPage<>(collector, nextPageUrl);
    }
}
