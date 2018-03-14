package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.exceptions.ParsingException;

public interface InfoItemExtractor {
    String getName() throws ParsingException;
    String getUrl() throws ParsingException;
    String getThumbnailUrl() throws ParsingException;
}
