package org.schabi.newpipe.extractor.suggestion;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.localization.ContentCountry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public abstract class SuggestionExtractor {
    private final StreamingService service;
    @Nullable private Locale forcedLocale;
    @Nullable private ContentCountry forcedContentCountry;

    public SuggestionExtractor(final StreamingService service) {
        this.service = service;
    }

    public abstract List<String> suggestionList(String query)
            throws IOException, ExtractionException;

    public int getServiceId() {
        return service.getServiceId();
    }

    public StreamingService getService() {
        return service;
    }

    // TODO: Create a more general Extractor class

    public void forceLocale(@Nullable final Locale locale) {
        this.forcedLocale = locale;
    }

    public void forceContentCountry(@Nullable final ContentCountry contentCountry) {
        this.forcedContentCountry = contentCountry;
    }

    @Nonnull
    public Locale getExtractorLocale() {
        return forcedLocale == null ? getService().getLocale() : forcedLocale;
    }

    @Nonnull
    public ContentCountry getExtractorContentCountry() {
        return forcedContentCountry == null
                ? getService().getContentCountry() : forcedContentCountry;
    }
}
