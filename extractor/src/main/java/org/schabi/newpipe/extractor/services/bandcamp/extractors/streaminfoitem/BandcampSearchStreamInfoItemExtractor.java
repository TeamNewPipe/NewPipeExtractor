package org.schabi.newpipe.extractor.services.bandcamp.extractors.streaminfoitem;

import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper;

import javax.annotation.Nullable;

public class BandcampSearchStreamInfoItemExtractor extends BandcampStreamInfoItemExtractor {

    private final Element resultInfo;
    private final Element searchResult;

    public BandcampSearchStreamInfoItemExtractor(final Element searchResult,
                                                 final String uploaderUrl) {
        super(uploaderUrl);
        this.searchResult = searchResult;
        resultInfo = searchResult.getElementsByClass("result-info").first();
    }

    @Override
    public String getUploaderName() {
        final String subhead = resultInfo.getElementsByClass("subhead").text();
        final String[] splitBy = subhead.split("by ");
        if (splitBy.length > 1) {
            return splitBy[1];
        } else {
            return splitBy[0];
        }
    }

    @Nullable
    @Override
    public String getUploaderAvatarUrl() {
        return null;
    }

    @Override
    public String getName() throws ParsingException {
        return resultInfo.getElementsByClass("heading").text();
    }

    @Override
    public String getUrl() throws ParsingException {
        return resultInfo.getElementsByClass("itemurl").text();
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return BandcampExtractorHelper.getThumbnailUrlFromSearchResult(searchResult);
    }

    @Override
    public long getDuration() {
        return -1;
    }
}
