package org.schabi.newpipe.extractor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public class Page implements Serializable {
    private final String url;
    private final List<String> ids;
    private final Map<String, String> cookies;

    public Page(final String url, final List<String> ids, final Map<String, String> cookies) {
        this.url = url;
        this.ids = ids;
        this.cookies = cookies;
    }

    public Page(final String url) {
        this(url, null, null);
    }

    public Page(final String url, final Map<String, String> cookies) {
        this(url, null, cookies);
    }

    public Page(final List<String> ids) {
        this(null, ids, null);
    }

    public Page(final List<String> ids, final Map<String, String> cookies) {
        this(null, ids, cookies);
    }

    public String getUrl() {
        return url;
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
