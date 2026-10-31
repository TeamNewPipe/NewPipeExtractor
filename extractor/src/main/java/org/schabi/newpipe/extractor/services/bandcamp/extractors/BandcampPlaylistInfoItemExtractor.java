package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor;

import javax.annotation.Nonnull;
import java.util.List;

import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getImagesFromSearchResult;

public class BandcampPlaylistInfoItemExtractor implements PlaylistInfoItemExtractor {
    private final Element searchResult;
    private final Element resultInfo;

    public BandcampPlaylistInfoItemExtractor(@Nonnull final Element searchResult) {
        this.searchResult = searchResult;
        resultInfo = searchResult.getElementsByClass("result-info").first();
    }

    @Override
    public String getUploaderName() {
        final String uploaderText =  resultInfo.getElementsByClass("subhead").text();
        if (uploaderText.startsWith("by ")) {
            return uploaderText.substring(3);
        }
        return uploaderText;
    }

    @Override
    public String getUploaderUrl() {
        return null;
    }

    @Override
    public boolean isUploaderVerified() {
        return false;
    }

    @Override
    public long getStreamCount() {
        final String length = resultInfo.getElementsByClass("length").text();
        int end = 0;
        while (end < length.length() && Character.isDigit(length.charAt(end))) {
            end++;
        }
        if (end == 0) {
            return ListExtractor.ITEM_COUNT_UNKNOWN;
        }
        return Long.parseLong(length.substring(0, end));
    }

    @Override
    public String getName() {
        return resultInfo.getElementsByClass("heading").text();
    }

    @Override
    public String getUrl() {
        return resultInfo.getElementsByClass("itemurl").text();
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() {
        return getImagesFromSearchResult(searchResult);
    }
}
