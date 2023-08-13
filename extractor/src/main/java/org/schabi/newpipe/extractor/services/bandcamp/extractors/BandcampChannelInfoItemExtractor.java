// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.channel.ChannelInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import javax.annotation.Nonnull;
import java.util.List;

import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getImagesFromSearchResult;

public class BandcampChannelInfoItemExtractor implements ChannelInfoItemExtractor {

    private final Element resultInfo;
    private final Element searchResult;

    public BandcampChannelInfoItemExtractor(final Element searchResult) {
        this.searchResult = searchResult;
        resultInfo = searchResult.getElementsByClass("result-info").first();
    }

    @Override
    public String getName() throws ParsingException {
        return resultInfo.getElementsByClass("heading").text();
    }

    @Override
    public String getUrl() throws ParsingException {
        return resultInfo.getElementsByClass("itemurl").text();
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() throws ParsingException {
        return getImagesFromSearchResult(searchResult);
    }

    @Override
    public String getDescription() {
        return resultInfo.getElementsByClass("subhead").text();
    }

    @Override
    public long getSubscriberCount() {
        return -1;
    }

    @Override
    public long getStreamCount() {
        return -1;
    }

    @Override
    public boolean isVerified() throws ParsingException {
        return false;
    }
}
