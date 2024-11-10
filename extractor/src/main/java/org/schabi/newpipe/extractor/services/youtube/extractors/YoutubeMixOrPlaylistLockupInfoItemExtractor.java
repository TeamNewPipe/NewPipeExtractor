package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistInfo;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubePlaylistLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nonnull;
import java.util.List;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.extractPlaylistTypeFromPlaylistId;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getImagesFromThumbnailsArray;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getUrlFromNavigationEndpoint;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.hasArtistOrVerifiedIconBadgeAttachment;

public class YoutubeMixOrPlaylistLockupInfoItemExtractor implements PlaylistInfoItemExtractor {

    @Nonnull
    private final JsonObject lockupViewModel;
    @Nonnull
    private final JsonObject thumbnailViewModel;
    @Nonnull
    private final JsonObject lockupMetadataViewModel;
    @Nonnull
    private final JsonObject firstMetadataRow;
    @Nonnull
    private PlaylistInfo.PlaylistType playlistType;

    public YoutubeMixOrPlaylistLockupInfoItemExtractor(@Nonnull final JsonObject lockupViewModel) {
        this.lockupViewModel = lockupViewModel;
        this.thumbnailViewModel = lockupViewModel.getObject("contentImage")
                .getObject("collectionThumbnailViewModel")
                .getObject("primaryThumbnail")
                .getObject("thumbnailViewModel");
        this.lockupMetadataViewModel = lockupViewModel.getObject("metadata")
                .getObject("lockupMetadataViewModel");
        /*
        The metadata rows are structured in the following way:
        1st part: uploader info, playlist type, playlist updated date
        2nd part: space row
        3rd element: first video
        4th (not always returned for playlists with less than 2 items?): second video
        5th element (always returned, but at a different index for playlists with less than 2
        items?): Show full playlist

        The first metadata row has the following structure:
        1st array element: uploader info
        2nd element: playlist type (course, playlist, podcast)
        3rd element (not always returned): playlist updated date
         */
        this.firstMetadataRow = lockupMetadataViewModel.getObject("metadata")
                .getObject("contentMetadataViewModel")
                .getArray("metadataRows")
                .getObject(0);

        try {
            this.playlistType = extractPlaylistTypeFromPlaylistId(getPlaylistId());
        } catch (final ParsingException e) {
            // If we cannot extract the playlist type, fall back to the normal one
            this.playlistType = PlaylistInfo.PlaylistType.NORMAL;
        }
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return firstMetadataRow.getArray("metadataParts")
                .getObject(0)
                .getObject("text")
                .getString("content");
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        if (playlistType != PlaylistInfo.PlaylistType.NORMAL) {
            // If the playlist is a mix, there is no uploader as they are auto-generated
            return null;
        }

        return getUrlFromNavigationEndpoint(
                firstMetadataRow.getArray("metadataParts")
                        .getObject(0)
                        .getObject("text")
                        .getArray("commandRuns")
                        .getObject(0)
                        .getObject("onTap")
                        .getObject("innertubeCommand"));
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        if (playlistType != PlaylistInfo.PlaylistType.NORMAL) {
            // If the playlist is a mix, there is no uploader as they are auto-generated
            return false;
        }

        return hasArtistOrVerifiedIconBadgeAttachment(
                firstMetadataRow.getArray("metadataParts")
                        .getObject(0)
                        .getObject("text")
                        .getArray("attachmentRuns"));
    }

    @Override
    public long getStreamCount() throws ParsingException {
        if (playlistType != PlaylistInfo.PlaylistType.NORMAL) {
            // If the playlist is a mix, we are not able to get its stream count
            return ListExtractor.ITEM_COUNT_INFINITE;
        }

        try {
            return Long.parseLong(Utils.removeNonDigitCharacters(
                    thumbnailViewModel.getArray("overlays")
                            .stream()
                            .filter(JsonObject.class::isInstance)
                            .map(JsonObject.class::cast)
                            .filter(overlay -> overlay.has("thumbnailOverlayBadgeViewModel"))
                            .findFirst()
                            .orElseThrow(() -> new ParsingException(
                                    "Could not get thumbnailOverlayBadgeViewModel"))
                            .getObject("thumbnailOverlayBadgeViewModel")
                            .getArray("thumbnailBadges")
                            .stream()
                            .filter(JsonObject.class::isInstance)
                            .map(JsonObject.class::cast)
                            .filter(badge -> badge.has("thumbnailBadgeViewModel"))
                            .findFirst()
                            .orElseThrow(() ->
                                    new ParsingException("Could not get thumbnailBadgeViewModel"))
                            .getObject("thumbnailBadgeViewModel")
                            .getString("text")));
        } catch (final Exception e) {
            throw new ParsingException("Could not get playlist stream count", e);
        }
    }

    @Override
    public String getName() throws ParsingException {
        return lockupMetadataViewModel.getObject("title")
                .getString("content");
    }

    @Override
    public String getUrl() throws ParsingException {
        // If the playlist item is a mix, we cannot return just its playlist ID as mix playlists
        // are not viewable in playlist pages
        // Use directly getUrlFromNavigationEndpoint in this case, which returns the watch URL with
        // the mix playlist
        if (playlistType == PlaylistInfo.PlaylistType.NORMAL) {
            try {
                return YoutubePlaylistLinkHandlerFactory.getInstance().getUrl(getPlaylistId());
            } catch (final Exception ignored) {
            }
        }

        return getUrlFromNavigationEndpoint(lockupViewModel.getObject("rendererContext")
                .getObject("commandContext")
                .getObject("onTap")
                .getObject("innertubeCommand"));
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() throws ParsingException {
        return getImagesFromThumbnailsArray(thumbnailViewModel.getObject("image")
                .getArray("sources"));
    }

    @Nonnull
    @Override
    public PlaylistInfo.PlaylistType getPlaylistType() throws ParsingException {
        return playlistType;
    }

    private String getPlaylistId() throws ParsingException {
        String id = lockupViewModel.getString("contentId");
        if (Utils.isNullOrEmpty(id)) {
            id = lockupViewModel.getObject("rendererContext")
                    .getObject("commandContext")
                    .getObject("watchEndpoint")
                    .getString("playlistId");
        }

        if (Utils.isNullOrEmpty(id)) {
            throw new ParsingException("Could not get playlist ID");
        }

        return id;
    }
}
