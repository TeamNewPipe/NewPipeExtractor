package org.schabi.newpipe.extractor.services.youtube.invidious.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousService;
import org.schabi.newpipe.extractor.services.youtube.shared.linkHandler.YoutubeLikeChannelLinkHandlerFactory;

import java.util.List;


public class InvidiousChannelLinkHandlerFactory extends YoutubeLikeChannelLinkHandlerFactory {

    protected final String baseUrl;

    public InvidiousChannelLinkHandlerFactory(final InvidiousService service) {
        super();
        baseUrl = service.getInstance().getUrl();
    }

    /**
     * Returns URL to channel from an ID
     *
     * @param id Channel ID including e.g. 'channel/'
     * @return URL to channel
     */
    @Override
    public String getUrl(final String id,
                         final List<String> contentFilters,
                         final String searchFilter) {
        return baseUrl + "/" + id;
    }

    @Override
    public String getId(final String url) throws ParsingException {
        final String id = super.getId(url);

        return "channel/" + id.split("/")[1];
    }
}
