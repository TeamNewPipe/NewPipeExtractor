package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor;

import java.io.IOException;

public class BandcampPlaylistInfoItemExtractor implements PlaylistInfoItemExtractor {

    private String title, artist, url, cover;
    private int trackCount;

    public BandcampPlaylistInfoItemExtractor(String title, String artist, String url, String cover, int trackCount) {
        this.title = title;
        this.artist = artist;
        this.url = url;
        this.cover = cover;
        this.trackCount = trackCount;
    }

    @Override
    public String getUploaderName() {
        return artist;
    }

    @Override
    public long getStreamCount() {
        return trackCount;
    }

    @Override
    public String getName() {
        return title;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getThumbnailUrl() {
        return cover;
    }
}
