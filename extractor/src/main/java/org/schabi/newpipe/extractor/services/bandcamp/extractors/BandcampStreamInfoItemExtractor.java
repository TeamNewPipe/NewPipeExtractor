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

    public BandcampStreamInfoItemExtractor(String title, String url, String cover, String artist) {
        this.title = title;
        this.url = url;
        this.cover = cover;
        this.artist = artist;
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        return StreamType.AUDIO_STREAM;
    }

    @Override
    public long getDuration() throws ParsingException {
        return -1;
    }

    @Override
    public long getViewCount() throws ParsingException {
        return -1;
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return artist;
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        return null;
    }

    @Nullable
    @Override
    public String getTextualUploadDate() throws ParsingException {
        return null; // TODO
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
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
