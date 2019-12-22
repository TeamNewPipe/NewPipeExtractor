// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import org.schabi.newpipe.extractor.channel.ChannelInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

public class BandcampChannelInfoItemExtractor implements ChannelInfoItemExtractor {

    private String name, url, image, location;

    public BandcampChannelInfoItemExtractor(String name, String url, String image, String location) {
        this.name = name;
        this.url = url;
        this.image = image;
        this.location = location;
    }

    @Override
    public String getName() throws ParsingException {
        return name;
    }

    @Override
    public String getUrl() throws ParsingException {
        return url;
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return image;
    }

    @Override
    public String getDescription() {
        return location;
    }

    @Override
    public long getSubscriberCount() {
        return -1;
    }

    @Override
    public long getStreamCount() {
        return -1;
    }
}
