package org.schabi.newpipe.extractor.services.bandcamp.extractors.streaminfoitem;

import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

import javax.annotation.Nonnull;
import java.util.List;

import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getImagesFromSearchResult;

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
    public long getDuration() {
        return -1;
    }
}
