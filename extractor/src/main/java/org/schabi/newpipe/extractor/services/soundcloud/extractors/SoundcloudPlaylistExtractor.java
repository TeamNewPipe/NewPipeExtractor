package org.schabi.newpipe.extractor.services.soundcloud.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public class SoundcloudPlaylistExtractor extends PlaylistExtractor {
    private static final int STREAMS_PER_REQUESTED_PAGE = 15;

    private String playlistId;
    private JsonObject playlist;

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
                    if (!isNullOrEmpty(artworkUrl)) break;
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
        return playlist.getLong("track_count");
    }

    @Nonnull
    @Override
    public String getSubChannelName() {
        return "";
    }

    @Nonnull
    @Override
    public String getSubChannelUrl() {
        return "";
    }

    @Nonnull
    @Override
    public String getSubChannelAvatarUrl() {
        return "";
    }

    public InfoItemsPage<StreamInfoItem> getInitialPage() {
        final StreamInfoItemsCollector streamInfoItemsCollector = new StreamInfoItemsCollector(getServiceId());
        final List<String> ids = new ArrayList<>();

        final JsonArray tracks = playlist.getArray("tracks");
        for (Object o : tracks) {
            if (o instanceof JsonObject) {
                final JsonObject track = (JsonObject) o;
                if (track.has("title")) { // i.e. if full info is available
                    streamInfoItemsCollector.commit(new SoundcloudStreamInfoItemExtractor(track));
                } else {
                    // %09d would be enough, but a 0 before the number does not create problems, so let's be sure
                    ids.add(String.format("%010d", track.getInt("id")));
                }
            }
        }

        return new InfoItemsPage<>(streamInfoItemsCollector, new Page(ids));
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(final Page page) throws IOException, ExtractionException {
        if (page == null || isNullOrEmpty(page.getIds())) {
            throw new IllegalArgumentException("Page doesn't contain IDs");
        }

        final List<String> currentIds;
        final List<String> nextIds;
        if (page.getIds().size() <= STREAMS_PER_REQUESTED_PAGE) {
            // Fetch every remaining stream, there are less than the max
            currentIds = page.getIds();
            nextIds = null;
        } else {
            currentIds = page.getIds().subList(0, STREAMS_PER_REQUESTED_PAGE);
            nextIds = page.getIds().subList(STREAMS_PER_REQUESTED_PAGE, page.getIds().size());
        }

        final String currentPageUrl = "https://api-v2.soundcloud.com/tracks?client_id="
                + SoundcloudParsingHelper.clientId()
                + "&ids=" + Utils.join(",", currentIds);

        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        final String response = NewPipe.getDownloader().get(currentPageUrl, getExtractorLocalization()).responseBody();

        try {
            final JsonArray tracks = JsonParser.array().from(response);
            for (Object track : tracks) {
                if (track instanceof JsonObject) {
                    collector.commit(new SoundcloudStreamInfoItemExtractor((JsonObject) track));
                }
            }
        } catch (JsonParserException e) {
            throw new ParsingException("Could not parse json response", e);
        }

        return new InfoItemsPage<>(collector, new Page(nextIds));
    }
}
