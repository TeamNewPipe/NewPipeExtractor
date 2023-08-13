package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static org.schabi.newpipe.extractor.ListExtractor.ITEM_COUNT_MORE_THAN_100;
import static org.schabi.newpipe.extractor.ListExtractor.ITEM_COUNT_UNKNOWN;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getImagesFromThumbnailsArray;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getUrlFromNavigationEndpoint;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.MUSIC_ALBUMS;
import static org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.MUSIC_PLAYLISTS;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public class YoutubeMusicAlbumOrPlaylistInfoItemExtractor implements PlaylistInfoItemExtractor {
    private final JsonObject albumOrPlaylistInfoItem;
    private final JsonArray descriptionElements;
    private final String searchType;

    public YoutubeMusicAlbumOrPlaylistInfoItemExtractor(final JsonObject albumOrPlaylistInfoItem,
                                                        final JsonArray descriptionElements,
                                                        final String searchType) {
        this.albumOrPlaylistInfoItem = albumOrPlaylistInfoItem;
        this.descriptionElements = descriptionElements;
        this.searchType = searchType;
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
        final String name;
        if (searchType.equals(MUSIC_ALBUMS)) {
            name = descriptionElements.getObject(2).getString("text");
        } else {
            name = descriptionElements.getObject(0).getString("text");
        }

        if (!isNullOrEmpty(name)) {
            return name;
        }

        throw new ParsingException("Could not get uploader name");
    }

    @Nullable
    @Override
    public String getUploaderUrl() throws ParsingException {
        if (searchType.equals(MUSIC_PLAYLISTS)) {
            return null;
        }

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

        throw new ParsingException("Could not get uploader URL");
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return false;
    }

    @Override
    public long getStreamCount() throws ParsingException {
        if (searchType.equals(MUSIC_ALBUMS)) {
            return ITEM_COUNT_UNKNOWN;
        }

        final String count = descriptionElements.getObject(2)
                .getString("text");

        if (!isNullOrEmpty(count)) {
            if (count.contains("100+")) {
                return ITEM_COUNT_MORE_THAN_100;
            } else {
                return Long.parseLong(Utils.removeNonDigitCharacters(count));
            }
        }

        throw new ParsingException("Could not get stream count");
    }
}
