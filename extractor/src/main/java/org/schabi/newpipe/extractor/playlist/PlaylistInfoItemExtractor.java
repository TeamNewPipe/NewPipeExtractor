package org.schabi.newpipe.extractor.playlist;

import org.schabi.newpipe.extractor.InfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import javax.annotation.Nonnull;

public interface PlaylistInfoItemExtractor extends InfoItemExtractor {

    /**
     * Get the uploader name
     * @return the uploader name
     */
    String getUploaderName() throws ParsingException;

    /**
     * Get the uploader url
     * @return the uploader url
     */
    String getUploaderUrl() throws ParsingException;

    /**
     * Get whether the uploader is verified
     * @return whether the uploader is verified
     */
    boolean isUploaderVerified() throws ParsingException;

    /**
     * Get the number of streams
     * @return the number of streams
     */
    long getStreamCount() throws ParsingException;

    /**
     * @return the type of this playlist, see {@link PlaylistInfo.PlaylistType} for a description
     *         of types. If not overridden always returns {@link PlaylistInfo.PlaylistType#NORMAL}.
     */
    @Nonnull
    default PlaylistInfo.PlaylistType getPlaylistType() throws ParsingException {
        return PlaylistInfo.PlaylistType.NORMAL;
    }
}
