package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItemExtractor;

import javax.annotation.Nonnull;

public class BandcampPlaylistInfoItemExtractor implements PlaylistInfoItemExtractor {
    private final Element searchResult;
    private final Element resultInfo;

    public BandcampPlaylistInfoItemExtractor(@Nonnull final Element searchResult) {
        this.searchResult = searchResult;
        resultInfo = searchResult.getElementsByClass("result-info").first();
    }

    @Override
    public String getUploaderName() {
        return resultInfo.getElementsByClass("subhead").text()
                .split(" by")[0];
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
        return Integer.parseInt(length.split(" track")[0]);
    }

    @Override
    public String getName() {
        return resultInfo.getElementsByClass("heading").text();
    }

    @Override
    public String getUrl() {
        return resultInfo.getElementsByClass("itemurl").text();
    }

    @Override
    public String getThumbnailUrl() {
        return BandcampExtractorHelper.getThumbnailUrlFromSearchResult(searchResult);
    }
}
