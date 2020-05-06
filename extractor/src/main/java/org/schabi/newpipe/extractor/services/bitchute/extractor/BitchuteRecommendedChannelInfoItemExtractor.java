package org.schabi.newpipe.extractor.services.bitchute.extractor;

import org.jsoup.nodes.Element;
import org.schabi.newpipe.extractor.channel.ChannelInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;

public class BitchuteRecommendedChannelInfoItemExtractor implements ChannelInfoItemExtractor {

    private final Element element;

    public BitchuteRecommendedChannelInfoItemExtractor(Element element) {
        this.element = element;
    }

    @Override
    public String getDescription() {
        return null;
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
    public String getName() throws ParsingException {
        try {
            return element.select(".channel-card-title").first().text();
        } catch (Exception e) {
            throw new ParsingException("Error parsing Channel title");
        }
    }

    @Override
    public String getUrl() throws ParsingException {
        try {
            return element.select("a")
                    .first().absUrl("href");
        } catch (Exception e) {
            throw new ParsingException("Error parsing Stream url");
        }
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        try {
            return element.select("a > img")
                    .first().attr("data-src");
        } catch (Exception e) {
            throw new ParsingException("Error parsing thumbnail url");
        }
    }

}
