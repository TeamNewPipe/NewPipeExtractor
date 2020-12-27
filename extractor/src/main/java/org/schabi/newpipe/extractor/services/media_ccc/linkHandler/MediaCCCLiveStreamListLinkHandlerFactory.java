package org.schabi.newpipe.extractor.services.media_ccc.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;

import java.util.List;
import java.util.regex.Pattern;

public class MediaCCCLiveStreamListLinkHandlerFactory extends ListLinkHandlerFactory {
    private static final String streamPattern = "^(https?://)?streaming.media.ccc.de$";

    @Override
    public String getId(String url) throws ParsingException {
        return "live";
    }

    @Override
    public boolean onAcceptUrl(String url) throws ParsingException {
        return Pattern.matches(streamPattern, url);
    }

    @Override
    public String getUrl(String id, List<String> contentFilter, String sortFilter) throws ParsingException {
        // FIXME: wrong URL; should be https://streaming.media.ccc.de/{conference_slug}/{room_slug}
        return "https://streaming.media.ccc.de/" + id;
    }
}
