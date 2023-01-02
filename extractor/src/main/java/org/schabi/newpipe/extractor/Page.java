package org.schabi.newpipe.extractor;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

/**
 * The {@link Page} class is used for storing information on future requests
 * for retrieving content.
 * <br>
 * A page has an {@link #id}, an {@link #url}, as well as information on possible {@link #cookies}.
 * In case the data behind the URL has already been retrieved,
 * it can be accessed by using {@link #getBody()} or {@link #getContent()}.
 */
public class Page implements Serializable {
    private final String url;
    private final String id;
    private final List<String> ids;
    private final Map<String, String> cookies;
    private Serializable content;

    @Nullable
    private final byte[] body;

    public Page(final String url,
                final String id,
                final List<String> ids,
                final Map<String, String> cookies,
                @Nullable final byte[] body) {
        this.url = url;
        this.id = id;
        this.ids = ids;
        this.cookies = cookies;
        this.body = body;
    }

    public Page(final String url) {
        this(url, null, null, null, null);
    }

    public Page(final String url, final String id) {
        this(url, id, null, null, null);
    }

    public Page(final String url, final byte[] body) {
        this(url, null, null, null, body);
    }

    public Page(final String url, final Map<String, String> cookies) {
        this(url, null, null, cookies, null);
    }

    public Page(final List<String> ids) {
        this(null, null, ids, null, null);
    }

    public Page(final List<String> ids, final Map<String, String> cookies) {
        this(null, null, ids, cookies, null);
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

    @Nullable
    public byte[] getBody() {
        return body;
    }

    public boolean hasContent() {
        return content != null;
    }

    /**
     * Get the page's content if it has been set, returns {@code null} otherwise.
     * @return the page's content
     */
    @Nullable
    public Serializable getContent() {
        return content;
    }

    /**
     * Set the page's content.
     * The page's content can either be retrieved manually by requesting the resource
     * behind the page's URL (see {@link #url} and {@link #getUrl()})
     * or storing it in a {@link Page}s instance in case the content has already been downloaded.
     * @param content the page's content
     */
    public void setContent(@Nullable final Serializable content) {
        this.content = content;
    }
}
