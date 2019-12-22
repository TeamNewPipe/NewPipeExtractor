// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import javax.annotation.Nullable;

public class BandcampStreamInfoItemExtractor implements StreamInfoItemExtractor {

    private String title;
    private String url;
    private String cover;
    private String artist;
    private long duration;

    public BandcampStreamInfoItemExtractor(String title, String url, String cover, String artist) {
        this(title, url, cover, artist, -1);
    }

    public BandcampStreamInfoItemExtractor(String title, String url, String cover, String artist, long duration) {
        this.title = title;
        this.url = url;
        this.cover = cover;
        this.artist = artist;
        this.duration = duration;
    }

    @Override
    public StreamType getStreamType() {
        return StreamType.AUDIO_STREAM;
    }

    @Override
    public long getDuration() {
        return duration;
    }

    @Override
    public long getViewCount() {
        return -1;
    }

    @Override
    public String getUploaderName() {
        return artist;
    }

    @Override
    public String getUploaderUrl() {
        return null;
    }

    @Nullable
    @Override
    public String getTextualUploadDate() {
        return null; // TODO
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() {
        return null;
    }

    @Override
    public String getName() throws ParsingException {
        return title;
    }

    @Override
    public String getUrl() throws ParsingException {
        return url;
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return cover;
    }

    /**
     * There are no ads just like that, duh
     */
    @Override
    public boolean isAd() throws ParsingException {
        return false;
    }
}
