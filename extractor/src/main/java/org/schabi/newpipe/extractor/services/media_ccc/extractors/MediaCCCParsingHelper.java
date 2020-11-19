package org.schabi.newpipe.extractor.services.media_ccc.extractors;

import org.schabi.newpipe.extractor.exceptions.ParsingException;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

public final class MediaCCCParsingHelper {
    private MediaCCCParsingHelper() { }

    public static OffsetDateTime parseDateFrom(final String textualUploadDate) throws ParsingException {
        try {
            return OffsetDateTime.parse(textualUploadDate);
        } catch (DateTimeParseException e) {
            throw new ParsingException("Could not parse date: \"" + textualUploadDate + "\"", e);
        }
    }
}
