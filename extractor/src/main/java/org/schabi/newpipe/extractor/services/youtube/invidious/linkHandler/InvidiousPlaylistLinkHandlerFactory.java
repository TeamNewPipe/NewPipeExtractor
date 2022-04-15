package org.schabi.newpipe.extractor.services.youtube.invidious.linkHandler;

import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousService;
import org.schabi.newpipe.extractor.services.youtube.shared.linkHandler.YoutubeLikePlaylistLinkHandlerFactory;

import java.util.List;

public class InvidiousPlaylistLinkHandlerFactory extends YoutubeLikePlaylistLinkHandlerFactory {

    protected final String baseUrl;

    public InvidiousPlaylistLinkHandlerFactory(final InvidiousService service) {
        this.baseUrl = service.getInstance().getUrl();
    }

    @Override
    public String getUrl(final String id, final List<String> contentFilters,
                         final String sortFilter) {
        return baseUrl +"/playlist?list=" + id;
    }

    @Override
    protected String getMixUrl(final String videoID, final String listID) {
        return baseUrl + "/watch?v=" + videoID + "&list=" + listID;
    }
}
