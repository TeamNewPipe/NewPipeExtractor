package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static org.schabi.newpipe.extractor.ListExtractor.ITEM_COUNT_UNKNOWN;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getImagesFromThumbnailsArray;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getUrlFromNavigationEndpoint;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.MUSIC_ALBUMS;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public class YoutubeMusicAlbumOrPlaylistInfoItemExtractor implements PlaylistInfoItemExtractor {
    private final JsonObject albumOrPlaylistInfoItem;
    private final JsonObject descriptionElementUploader;

    public YoutubeMusicAlbumOrPlaylistInfoItemExtractor(final JsonObject albumOrPlaylistInfoItem,
                                                        final JsonArray descriptionElements,
                                                        final String searchType) {
        this.albumOrPlaylistInfoItem = albumOrPlaylistInfoItem;

        if (searchType.equals(MUSIC_ALBUMS)) {
            // "Album", " • ", uploader, " • ", year
            this.descriptionElementUploader = descriptionElements.getObject(2);
        } else {
            // uploader, " • ", view count
            this.descriptionElementUploader = descriptionElements.getObject(0);
        }
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() throws ParsingException {
        try {
            return getImagesFromThumbnailsArray(
                    albumOrPlaylistInfoItem.getObject("thumbnail")
                            .getObject("musicThumbnailRenderer")
                            .getObject("thumbnail")
                            .getArray("thumbnails"));
        } catch (final Exception e) {
            throw new ParsingException("Could not get thumbnails", e);
        }
    }

    @Override
    public String getName() throws ParsingException {
        final String name = getTextFromObject(albumOrPlaylistInfoItem.getArray("flexColumns")
                .getObject(0)
                .getObject("musicResponsiveListItemFlexColumnRenderer")
                .getObject("text"));

        if (!isNullOrEmpty(name)) {
            return name;
        }

        throw new ParsingException("Could not get name");
    }

    @Override
    public String getUrl() throws ParsingException {
        String playlistId = albumOrPlaylistInfoItem.getObject("menu")
                .getObject("menuRenderer")
                .getArray("items")
                .getObject(4)
                .getObject("toggleMenuServiceItemRenderer")
                .getObject("toggledServiceEndpoint")
                .getObject("likeEndpoint")
                .getObject("target")
                .getString("playlistId");

        if (isNullOrEmpty(playlistId)) {
            playlistId = albumOrPlaylistInfoItem.getObject("overlay")
                    .getObject("musicItemThumbnailOverlayRenderer")
                    .getObject("content")
                    .getObject("musicPlayButtonRenderer")
                    .getObject("playNavigationEndpoint")
                    .getObject("watchPlaylistEndpoint")
                    .getString("playlistId");
        }

        if (!isNullOrEmpty(playlistId)) {
            return "https://music.youtube.com/playlist?list=" + playlistId;
        }

        throw new ParsingException("Could not get URL");
    }

    @Override
    public String getUploaderName() throws ParsingException {
        final String name = descriptionElementUploader.getString("text");

        if (!isNullOrEmpty(name)) {
            return name;
        }

        throw new ParsingException("Could not get uploader name");
    }

    @Nullable
    @Override
    public String getUploaderUrl() throws ParsingException {
        // first try obtaining the uploader from the menu (will not work for MUSIC_PLAYLISTS though)
        final JsonArray items = albumOrPlaylistInfoItem.getObject("menu")
                .getObject("menuRenderer")
                .getArray("items");
        for (final Object item : items) {
            final JsonObject menuNavigationItemRenderer =
                    ((JsonObject) item).getObject("menuNavigationItemRenderer");
            if (menuNavigationItemRenderer.getObject("icon")
                    .getString("iconType", "")
                    .equals("ARTIST")) {
                return getUrlFromNavigationEndpoint(
                        menuNavigationItemRenderer.getObject("navigationEndpoint"));
            }
        }

        // then try obtaining it from the uploader description element
        if (descriptionElementUploader.has("navigationEndpoint")) {
            return getUrlFromNavigationEndpoint(
                    descriptionElementUploader.getObject("navigationEndpoint"));
        } else {
            // if there is no navigationEndpoint for the uploader
            // then this playlist/album is likely autogenerated
            return null;
        }
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return false;
    }

    @Override
    public long getStreamCount() throws ParsingException {
        // YouTube Music album and playlist info items don't expose the stream count anywhere...
        return ITEM_COUNT_UNKNOWN;
    }
}
