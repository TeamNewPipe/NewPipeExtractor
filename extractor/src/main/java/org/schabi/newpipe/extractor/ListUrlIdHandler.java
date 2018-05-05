package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.exceptions.ParsingException;

public abstract class ListUrlIdHandler extends UrlIdHandler {

    public abstract String getUrl(String id, String[] contentFilter, String sortFilter) throws ParsingException;

    @Override
    public String getUrl(String id) throws ParsingException {
        return getUrl(id, new String[0], null);
    }

    /**
     * Will returns content filter the corresponding extractor can handle like "channels", "videos", "music", etc.
     *
     * @return filter that can be applied when building a query for getting a list
     */
    public String[] getAvailableContentFilter() {
        return new String[0];
    }

    /**
     * Will returns sort filter the corresponding extractor can handle like "A-Z", "oldest first", "size", etc.
     *
     * @return filter that can be applied when building a query for getting a list
     */
    public String[] getAvailableSortFilter() {
        return new String[0];
    }
}
