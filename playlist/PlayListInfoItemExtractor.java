package org.schabi.newpipe.extractor.playlist;

import org.schabi.newpipe.extractor.exceptions.ParsingException;

public interface PlayListInfoItemExtractor {
    String getThumbnailUrl() throws ParsingException;
    String getPlayListName() throws ParsingException;
    String getWebPageUrl() throws ParsingException;
}
