package org.schabi.newpipe.extractor.playlist;

import org.schabi.newpipe.extractor.InfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

public interface PlaylistInfoItemExtractor extends InfoItemExtractor {
    String getUploaderName() throws ParsingException;
    long getStreamCount() throws ParsingException;
}
