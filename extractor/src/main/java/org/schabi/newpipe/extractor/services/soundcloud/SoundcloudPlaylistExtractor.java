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

    @Nullable
    @Override
    public String getThumbnailUrl() {
        String artworkUrl = playlist.getString("artwork_url");

        if (artworkUrl == null) {
            // If the thumbnail is null, traverse the items list and get a valid one,
            // if it also fails, return null
            try {
                final InfoItemsPage<StreamInfoItem> infoItems = getInitialPage();

                for (StreamInfoItem item : infoItems.getItems()) {
                    artworkUrl = item.getThumbnailUrl();
                    if (artworkUrl != null && !artworkUrl.isEmpty()) break;
                }
            } catch (Exception ignored) {
            }

            if (artworkUrl == null) {
                return null;
            }
        }

        return artworkUrl.replace("large.jpg", "crop.jpg");
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
            computeInitialTracksAndNextPageUrl();
        }
        return new InfoItemsPage<>(streamInfoItemsCollector, nextPageUrl);
    }

    private void computeInitialTracksAndNextPageUrl() throws IOException, ExtractionException {
        streamInfoItemsCollector = new StreamInfoItemsCollector(getServiceId());
        StringBuilder nextPageUrlBuilder = new StringBuilder("https://api-v2.soundcloud.com/tracks?client_id=");
        nextPageUrlBuilder.append(SoundcloudParsingHelper.clientId());
        nextPageUrlBuilder.append("&ids=");

        JsonArray tracks = playlist.getArray("tracks");
        for (Object o : tracks) {
            if (o instanceof JsonObject) {
                JsonObject track = (JsonObject) o;
                if (track.has("title")) { // i.e. if full info is available
                    streamInfoItemsCollector.commit(new SoundcloudStreamInfoItemExtractor(track));
                } else {
                    // %09d would be enough, but a 0 before the number does not create problems, so let's be sure
                    nextPageUrlBuilder.append(String.format("%010d,", track.getInt("id")));
                }
            }
        }

        nextPageUrl = nextPageUrlBuilder.toString();
        if (nextPageUrl.endsWith("&ids=")) {
            // there are no other videos
            nextPageUrl = "";
        }
    }

    @Override
    public String getNextPageUrl() throws IOException, ExtractionException {
        if (nextPageUrl == null) {
            computeInitialTracksAndNextPageUrl();
        }
        return nextPageUrl;
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(String pageUrl) throws IOException, ExtractionException {
        if (pageUrl == null || pageUrl.isEmpty()) {
            throw new ExtractionException(new IllegalArgumentException("Page url is empty or null"));
        }

        // see computeInitialTracksAndNextPageUrl
        final int lengthFirstPartOfUrl = ("https://api-v2.soundcloud.com/tracks?client_id="
                + SoundcloudParsingHelper.clientId()
                + "&ids=").length();
        final int lengthOfEveryStream = 11;

        String currentPageUrl;
        int lengthMaxStreams = lengthFirstPartOfUrl + lengthOfEveryStream * streamsPerRequestedPage;
        if (pageUrl.length() <= lengthMaxStreams) {
            currentPageUrl = pageUrl; // fetch every remaining video, there are less than the max
            nextPageUrl = ""; // afterwards the list is complete
        } else {
            currentPageUrl = pageUrl.substring(0, lengthMaxStreams);
            nextPageUrl = pageUrl.substring(0, lengthFirstPartOfUrl) + pageUrl.substring(lengthMaxStreams);
        }

        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        String response = NewPipe.getDownloader().get(currentPageUrl, getExtractorLocalization()).responseBody();

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

        return new InfoItemsPage<>(collector, nextPageUrl);
    }
}
