// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import javax.annotation.Nullable;
import java.io.IOException;

public class BandcampStreamInfoItemExtractor implements StreamInfoItemExtractor {

    private String title;
    private String url;
    private String cover;
    private String artist;
    private long duration;
    private StreamingService service;

    public BandcampStreamInfoItemExtractor(String title, String url, String artist, long duration, StreamingService service) {
        this(title, url, null, artist, duration);
        this.service = service;
    }

    public BandcampStreamInfoItemExtractor(String title, String url, String cover, String artist) {
        this(title, url, cover, artist, -1);
    }

    public BandcampStreamInfoItemExtractor(Element searchResult) {
        Element resultInfo = searchResult.getElementsByClass("result-info").first();

        Element img = searchResult.getElementsByClass("art").first()
                .getElementsByTag("img").first();
        if (img != null) {
            cover = img.attr("src");
        }

        title = resultInfo.getElementsByClass("heading").text();
        url = resultInfo.getElementsByClass("itemurl").text();

        String subhead = resultInfo.getElementsByClass("subhead").text();
        String[] splitBy = subhead.split(" by");
        if (splitBy.length > 1) {
            artist = subhead.split(" by")[1];
        }
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

    /**
     * There is no guarantee that every track of an album has the same cover art, so it needs to be fetched
     * per-track if in playlist view
     */
    @Override
    public String getThumbnailUrl() throws ParsingException {
        if (cover != null) return cover;
        else {
            try {
                StreamExtractor extractor = service.getStreamExtractor(getUrl());
                extractor.fetchPage();
                return extractor.getThumbnailUrl();
            } catch (ExtractionException | IOException e) {
                throw new ParsingException("could not download cover art location", e);
            }
        }
    }

    /**
     * There are no ads just like that, duh
     */
    @Override
    public boolean isAd() throws ParsingException {
        return false;
    }
}
