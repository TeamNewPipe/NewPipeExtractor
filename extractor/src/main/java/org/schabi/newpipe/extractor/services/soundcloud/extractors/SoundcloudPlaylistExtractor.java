package org.schabi.newpipe.extractor.services.soundcloud.extractors;

import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL;
import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.getAllImagesFromArtworkOrAvatarUrl;
import static org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper.getAvatarUrl;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudParsingHelper;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class SoundcloudPlaylistExtractor extends PlaylistExtractor {
    private static final int STREAMS_PER_REQUESTED_PAGE = 15;

    private String playlistId;
    private JsonObject playlist;

    public SoundcloudPlaylistExtractor(final StreamingService service,
                                       final ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) throws IOException,
            ExtractionException {

        playlistId = getLinkHandler().getId();
        final String apiUrl = SOUNDCLOUD_API_V2_URL + "playlists/" + playlistId + "?client_id="
                + SoundcloudParsingHelper.clientId() + "&representation=compact";

        final String response = downloader.get(apiUrl, getExtractorLocalization()).responseBody();
        try {
            playlist = JsonParser.object().from(response);
        } catch (final JsonParserException e) {
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

    @Nonnull
    @Override
    public List<Image> getThumbnails() {
        final String artworkUrl = playlist.getString("artwork_url");

        if (!isNullOrEmpty(artworkUrl)) {
            return getAllImagesFromArtworkOrAvatarUrl(artworkUrl);
        }

        // If the thumbnail is null or empty, traverse the items list and get a valid one
        // If it also fails, return an empty list
        try {
            final InfoItemsPage<StreamInfoItem> infoItems = getInitialPage();

            for (final StreamInfoItem item : infoItems.getItems()) {
                final List<Image> thumbnails = item.getThumbnails();
                if (!isNullOrEmpty(thumbnails)) {
                    return thumbnails;
                }
            }
        } catch (final Exception ignored) {
        }

        return List.of();
    }

    @Override
    public String getUploaderUrl() {
        return SoundcloudParsingHelper.getUploaderUrl(playlist);
    }

    @Override
    public String getUploaderName() {
        return SoundcloudParsingHelper.getUploaderName(playlist);
    }

    @Nonnull
    @Override
    public List<Image> getUploaderAvatars() {
        return getAllImagesFromArtworkOrAvatarUrl(getAvatarUrl(playlist));
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return playlist.getObject("user").getBoolean("verified");
    }

    @Override
    public long getStreamCount() {
        return playlist.getLong("track_count");
    }

    @Nonnull
    @Override
    public Description getDescription() throws ParsingException {
        final String description = playlist.getString("description");
        if (isNullOrEmpty(description)) {
            return Description.EMPTY_DESCRIPTION;
        }
        return new Description(description, Description.PLAIN_TEXT);
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() {
        final StreamInfoItemsCollector streamInfoItemsCollector =
                new StreamInfoItemsCollector(getServiceId());
        final List<String> ids = new ArrayList<>();

        playlist.getArray("tracks")
                .stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .forEachOrdered(track -> {
                    // i.e. if full info is available
                    if (track.has("title")) {
                        streamInfoItemsCollector.commit(
                                new SoundcloudStreamInfoItemExtractor(track));
                    } else {
                        // %09d would be enough, but a 0 before the number does not create
                        // problems, so let's be sure
                        ids.add(String.format("%010d", track.getInt("id")));
                    }
                });

        return new InfoItemsPage<>(streamInfoItemsCollector, new Page(ids));
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(final Page page) throws IOException,
            ExtractionException {
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

        final String currentPageUrl = SOUNDCLOUD_API_V2_URL + "tracks?client_id="
                + SoundcloudParsingHelper.clientId() + "&ids=" + String.join(",", currentIds);

        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        final String response = NewPipe.getDownloader().get(currentPageUrl,
                getExtractorLocalization()).responseBody();

        try {
            final JsonArray tracks = JsonParser.array().from(response);
            // Response may not contain tracks in the same order as currentIds.
            // The streams are displayed in the order which is used in currentIds on SoundCloud.
            final HashMap<Integer, JsonObject> idToTrack = new HashMap<>();
            for (final Object track : tracks) {
                if (track instanceof JsonObject) {
                    final JsonObject o = (JsonObject) track;
                    idToTrack.put(o.getInt("id"), o);
                }
            }
            for (final String strId : currentIds) {
                final int id = Integer.parseInt(strId);
                try {
                    collector.commit(new SoundcloudStreamInfoItemExtractor(
                        Objects.requireNonNull(
                                idToTrack.get(id),
                        "no track with id " + id + " in response"
                        )
                    ));
                } catch (final NullPointerException e) {
                    throw new ParsingException("Could not parse json response", e);
                }
            }
        } catch (final JsonParserException e) {
            throw new ParsingException("Could not parse json response", e);
        }

        return new InfoItemsPage<>(collector, new Page(nextIds));
    }
}
