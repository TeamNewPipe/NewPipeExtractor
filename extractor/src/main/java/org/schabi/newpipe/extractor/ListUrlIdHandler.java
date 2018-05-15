package org.schabi.newpipe.extractor;

import org.schabi.newpipe.extractor.exceptions.ParsingException;

public abstract class ListUrlIdHandler extends UrlIdHandler {

    protected String[] contentFilter;
    protected String sortFilter;

    public ListUrlIdHandler setQuery(String id, String[] contentFilter, String softFilter) throws ParsingException {
        setId(id);
        this.contentFilter = contentFilter;
        this.sortFilter = softFilter;
        return this;
    }

    public ListUrlIdHandler setUrl(String url) throws ParsingException {
        return (ListUrlIdHandler) super.setUrl(url);
    }

    public ListUrlIdHandler setId(String id) throws ParsingException {
        return (ListUrlIdHandler) super.setId(id);
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
