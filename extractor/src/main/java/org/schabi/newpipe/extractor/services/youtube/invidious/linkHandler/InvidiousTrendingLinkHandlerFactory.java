package org.schabi.newpipe.extractor.services.youtube.invidious.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InvidiousTrendingLinkHandlerFactory extends ListLinkHandlerFactory {
    public static final Map<String, String> KIOSK_MAP;
    public static final String KIOSK_TRENDING = "Trending";
    public static final String KIOSK_POPULAR = "Popular";

    static {
        final Map<String, String> map = new HashMap<>();
        // https://docs.invidious.io/api/#get-apiv1trending
        map.put(KIOSK_TRENDING, "%s/api/v1/trending");
        // https://docs.invidious.io/api/#get-apiv1popular
        map.put(KIOSK_POPULAR, "%s/api/v1/popular");

        KIOSK_MAP = Collections.unmodifiableMap(map);
    }

    protected final String baseUrl;

    public InvidiousTrendingLinkHandlerFactory(final InvidiousService service) {
        this.baseUrl = service.getInstance().getUrl();
    }

    @Override
    public String getUrl(final String id,
                         final List<String> contentFilters,
                         final String sortFilter) {
        return getUrl(id, contentFilters, sortFilter, baseUrl);
    }

    @Override
    public String getUrl(final String id,
                         final List<String> contentFilters,
                         final String sortFilter,
                         @SuppressWarnings("HiddenField") final String baseUrl) {
        return String.format(KIOSK_MAP.get(id), baseUrl);
    }

    @Override
    public String getId(final String url) throws ParsingException {
        final String cleanUrl = url.replace(baseUrl, "%s");
        if (cleanUrl.contains("/trending")) {
            return KIOSK_TRENDING;
        } else if (cleanUrl.contains("/popular")) {
            return KIOSK_POPULAR;
        }
        throw new ParsingException("no id found for this url");
    }

    @Override
    public boolean onAcceptUrl(final String url) {
        return url.contains("/trending")
                || url.contains("/popular");
    }
}
