package org.schabi.newpipe.extractor.services.peertube.linkHandler;

import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PeertubeTrendingLinkHandlerFactory extends ListLinkHandlerFactory {


    private static final PeertubeTrendingLinkHandlerFactory instance = new PeertubeTrendingLinkHandlerFactory();

    public static final Map<String, String> KIOSK_MAP;
    public static final Map<String, String> REVERSE_KIOSK_MAP;
    public static final String KIOSK_TRENDING = "Trending";
    public static final String KIOSK_MOST_LIKED = "Most liked";
    public static final String KIOSK_RECENT = "Recently added";
    public static final String KIOSK_LOCAL = "Local";

    static {
        Map<String, String> map = new HashMap<>();
        map.put(KIOSK_TRENDING, "%s/api/v1/videos?sort=-trending");
        map.put(KIOSK_MOST_LIKED, "%s/api/v1/videos?sort=-likes");
        map.put(KIOSK_RECENT, "%s/api/v1/videos?sort=-publishedAt");
        map.put(KIOSK_LOCAL, "%s/api/v1/videos?sort=-publishedAt&filter=local");
        KIOSK_MAP = Collections.unmodifiableMap(map);

        Map<String, String> reverseMap = new HashMap<>();
        for (Map.Entry<String, String> entry : KIOSK_MAP.entrySet()) {
            reverseMap.put(entry.getValue(), entry.getKey());
        }
        REVERSE_KIOSK_MAP = Collections.unmodifiableMap(reverseMap);
    }

    public static PeertubeTrendingLinkHandlerFactory getInstance() {
        return instance;
    }

    @Override
    public String getUrl(String id, List<String> contentFilters, String sortFilter) {
        String baseUrl = ServiceList.PeerTube.getBaseUrl();
        return getUrl(id, contentFilters, sortFilter, baseUrl);
    }

    @Override
    public String getUrl(String id, List<String> contentFilters, String sortFilter, String baseUrl) {
        return String.format(KIOSK_MAP.get(id), baseUrl);
    }

    @Override
    public String getId(String url) throws ParsingException {
        String baseUrl = ServiceList.PeerTube.getBaseUrl();
        url = url.replace(baseUrl, "%s");
        if (url.contains("/videos/trending")) {
            return KIOSK_TRENDING;
        } else if (url.contains("/videos/most-liked")) {
            return KIOSK_MOST_LIKED;
        } else if (url.contains("/videos/recently-added")) {
            return KIOSK_RECENT;
        } else if (url.contains("/videos/local")) {
            return KIOSK_LOCAL;
        } else if (REVERSE_KIOSK_MAP.containsKey(url)) {
            return REVERSE_KIOSK_MAP.get(url);
        } else {
            throw new ParsingException("no id found for this url");
        }
    }

    @Override
    public boolean onAcceptUrl(final String url) {
        return url.contains("/videos?") || url.contains("/videos/trending") || url.contains("/videos/most-liked") || url.contains("/videos/recently-added") || url.contains("/videos/local");
    }
}
