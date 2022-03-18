package org.schabi.newpipe.extractor.services.peertube.linkHandler;

import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PeertubeTrendingLinkHandlerFactory extends ListLinkHandlerFactory {

    private static final PeertubeTrendingLinkHandlerFactory INSTANCE
            = new PeertubeTrendingLinkHandlerFactory();

    public static final Map<String, String> KIOSK_MAP;
    public static final Map<String, String> REVERSE_KIOSK_MAP;
    public static final String KIOSK_TRENDING = "Trending";
    public static final String KIOSK_MOST_LIKED = "Most liked";
    public static final String KIOSK_RECENT = "Recently added";
    public static final String KIOSK_LOCAL = "Local";

    static {
        final Map<String, String> map = new HashMap<>();
        map.put(KIOSK_TRENDING, "%s/api/v1/videos?sort=-trending");
        map.put(KIOSK_MOST_LIKED, "%s/api/v1/videos?sort=-likes");
        map.put(KIOSK_RECENT, "%s/api/v1/videos?sort=-publishedAt");
        map.put(KIOSK_LOCAL, "%s/api/v1/videos?sort=-publishedAt&filter=local");
        KIOSK_MAP = Collections.unmodifiableMap(map);

        final Map<String, String> reverseMap = new HashMap<>();
        for (final Map.Entry<String, String> entry : KIOSK_MAP.entrySet()) {
            reverseMap.put(entry.getValue(), entry.getKey());
        }
        REVERSE_KIOSK_MAP = Collections.unmodifiableMap(reverseMap);
    }

    public static PeertubeTrendingLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public String getUrl(final String id,
                         final List<String> contentFilters,
                         final String sortFilter) {
        return getUrl(id, contentFilters, sortFilter, ServiceList.PeerTube.getBaseUrl());
    }

    @Override
    public String getUrl(final String id,
                         final List<String> contentFilters,
                         final String sortFilter,
                         final String baseUrl) {
        return String.format(KIOSK_MAP.get(id), baseUrl);
    }

    @Override
    public String getId(final String url) throws ParsingException {
        final String cleanUrl = url.replace(ServiceList.PeerTube.getBaseUrl(), "%s");
        if (cleanUrl.contains("/videos/trending")) {
            return KIOSK_TRENDING;
        } else if (cleanUrl.contains("/videos/most-liked")) {
            return KIOSK_MOST_LIKED;
        } else if (cleanUrl.contains("/videos/recently-added")) {
            return KIOSK_RECENT;
        } else if (cleanUrl.contains("/videos/local")) {
            return KIOSK_LOCAL;
        } else if (REVERSE_KIOSK_MAP.containsKey(cleanUrl)) {
            return REVERSE_KIOSK_MAP.get(cleanUrl);
        } else {
            throw new ParsingException("no id found for this url");
        }
    }

    @Override
    public boolean onAcceptUrl(final String url) {
        return url.contains("/videos?") || url.contains("/videos/trending")
                || url.contains("/videos/most-liked") || url.contains("/videos/recently-added")
                || url.contains("/videos/local");
    }
}
