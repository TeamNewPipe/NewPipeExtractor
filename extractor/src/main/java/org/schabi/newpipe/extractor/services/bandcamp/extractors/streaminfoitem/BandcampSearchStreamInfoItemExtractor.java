package org.schabi.newpipe.extractor.services.bandcamp.extractors.streaminfoitem;

import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

public class BandcampSearchStreamInfoItemExtractor extends BandcampStreamInfoItemExtractor {

    private final Element resultInfo, searchResult;

    public BandcampSearchStreamInfoItemExtractor(Element searchResult, String uploaderUrl) {
        super(uploaderUrl);
        this.searchResult = searchResult;
        resultInfo = searchResult.getElementsByClass("result-info").first();
    }

    @Override
    public String getUploaderName() {
        String subhead = resultInfo.getElementsByClass("subhead").text();
        String[] splitBy = subhead.split("by ");
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

    @Override
    public String getThumbnailUrl() throws ParsingException {
        Element img = searchResult.getElementsByClass("art").first()
                .getElementsByTag("img").first();
        if (img != null) {
            return img.attr("src");
        } else return null;
    }

    @Override
    public long getDuration() {
        return -1;
    }
}
