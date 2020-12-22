package org.schabi.newpipe.extractor.services.media_ccc.linkHandler;

import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;

import java.util.List;
import java.util.regex.Pattern;

public class MediaCCCRecentListLinkHandlerFactory extends ListLinkHandlerFactory {
    @Override
    public String getId(String url) {
        return "recent";
    }

    @Override
    public boolean onAcceptUrl(String url) {
        final String pattern = "^(https?://)?media.ccc.de/recent/?$";
        return Pattern.matches(pattern, url);
    }

    @Override
    public String getUrl(String id, List<String> contentFilter, String sortFilter) {
        return "https://media.ccc.de/recent";
    }
}
