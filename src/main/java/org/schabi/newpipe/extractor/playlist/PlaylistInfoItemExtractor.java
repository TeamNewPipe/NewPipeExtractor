package org.schabi.newpipe.extractor.playlist;

import org.schabi.newpipe.extractor.exceptions.ParsingException;

public interface PlaylistInfoItemExtractor {
    String getThumbnailUrl() throws ParsingException;
    String getUploaderName() throws ParsingException;
    String getPlaylistName() throws ParsingException;
    String getWebPageUrl() throws ParsingException;
    long getStreamCount() throws ParsingException;
}
