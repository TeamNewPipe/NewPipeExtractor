package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor;

public class BandcampPlaylistInfoItemExtractor implements PlaylistInfoItemExtractor {

    private String title, artist, url, cover;

    public BandcampPlaylistInfoItemExtractor(String title, String artist, String url, String cover) {
        this.title = title;
        this.artist = artist;
        this.url = url;
        this.cover = cover;
    }

    @Override
    public String getUploaderName() {
        return artist;
    }

    @Override
    public long getStreamCount() {
        return -1;
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
