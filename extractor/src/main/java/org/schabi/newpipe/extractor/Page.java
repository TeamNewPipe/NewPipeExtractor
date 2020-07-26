package org.schabi.newpipe.extractor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public class Page implements Serializable {
    private final String url;
    private final String id;
    private final List<String> ids;
    private final Map<String, String> cookies;

    public Page(final String url, final String id, final List<String> ids, final Map<String, String> cookies) {
        this.url = url;
        this.id = id;
        this.ids = ids;
        this.cookies = cookies;
    }

    public Page(final String url) {
        this(url, null, null, null);
    }

    public Page(final String url, final String id) {
        this(url, id, null, null);
    }

    public Page(final String url, final Map<String, String> cookies) {
        this(url, null, null, cookies);
    }

    public Page(final List<String> ids) {
        this(null, null, ids, null);
    }

    public Page(final List<String> ids, final Map<String, String> cookies) {
        this(null, null, ids, cookies);
    }

    public String getUrl() {
        return url;
    }

    public String getId() {
        return id;
    }

    public List<String> getIds() {
        return ids;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public static boolean isValid(final Page page) {
        return page != null && (!isNullOrEmpty(page.getUrl())
                || !isNullOrEmpty(page.getIds()));
    }
}
