package org.schabi.newpipe.extractor.services.soundcloud.kiosk;

import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Parser;

import java.util.List;

public class JedenTagEinSetLinkHandlerFactory extends ListLinkHandlerFactory {
    public static final String URL = "https://jedentageinset.de";

    @Override
    public String getId(String url) {
        return "jedentageinset";
    }

    @Override
    public String getUrl(String id, List<String> contentFilter, String sortFilter) {
        return URL;
    }

    @Override
    public boolean onAcceptUrl(final String url) {
        return url.startsWith(URL);
    }
}
