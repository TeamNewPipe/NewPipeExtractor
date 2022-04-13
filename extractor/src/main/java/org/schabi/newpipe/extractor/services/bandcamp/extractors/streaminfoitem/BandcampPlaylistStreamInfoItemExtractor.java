// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.extractors.streaminfoitem;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.stream.StreamExtractor;

import javax.annotation.Nullable;
import java.io.IOException;


public class BandcampPlaylistStreamInfoItemExtractor extends BandcampStreamInfoItemExtractor {

    private final JsonObject track;
    private String substituteCoverUrl;
    private final StreamingService service;

    public BandcampPlaylistStreamInfoItemExtractor(final JsonObject track,
                                                   final String uploaderUrl,
                                                   final StreamingService service) {
        super(uploaderUrl);
        this.track = track;
        this.service = service;
    }

    public BandcampPlaylistStreamInfoItemExtractor(final JsonObject track,
                                                   final String uploaderUrl,
                                                   final String substituteCoverUrl) {
        this(track, uploaderUrl, (StreamingService) null);
        this.substituteCoverUrl = substituteCoverUrl;
    }

    @Override
    public String getName() {
        return track.getString("title");
    }

    @Override
    public String getUrl() {
        return getUploaderUrl() + track.getString("title_link");
    }

    @Override
    public long getDuration() {
        return track.getLong("duration");
    }

    @Override
    public String getUploaderName() {
        /* Tracks can have an individual artist name, but it is not included in the
         * given JSON.
         */
        return "";
    }

    @Nullable
    @Override
    public String getUploaderAvatarUrl() {
        return null;
    }

    /**
     * Each track can have its own cover art. Therefore, unless a substitute is provided,
     * the thumbnail is extracted using a stream extractor.
     */
    @Override
    public String getThumbnailUrl() throws ParsingException {
        if (substituteCoverUrl != null) {
            return substituteCoverUrl;
        } else {
            try {
                final StreamExtractor extractor = service.getStreamExtractor(getUrl());
                extractor.fetchPage();
                return extractor.getThumbnailUrl();
            } catch (final ExtractionException | IOException e) {
                throw new ParsingException("could not download cover art location", e);
            }
        }
    }
}
