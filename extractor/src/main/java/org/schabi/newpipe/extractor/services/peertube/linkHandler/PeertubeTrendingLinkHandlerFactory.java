package org.schabi.newpipe.extractor.services.peertube.linkHandler;

import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;

import java.util.List;
import java.util.Map;

public final class PeertubeTrendingLinkHandlerFactory extends ListLinkHandlerFactory {

    private static final PeertubeTrendingLinkHandlerFactory INSTANCE
            = new PeertubeTrendingLinkHandlerFactory();

    public static final String KIOSK_TRENDING = "Trending";
    public static final String KIOSK_MOST_LIKED = "Most liked";
    public static final String KIOSK_RECENT = "Recently added";
    public static final String KIOSK_LOCAL = "Local";

    public static final Map<String, String> KIOSK_MAP = Map.of(
            KIOSK_TRENDING, "%s/api/v1/videos?sort=-trending",
            KIOSK_MOST_LIKED, "%s/api/v1/videos?sort=-likes",
            KIOSK_RECENT, "%s/api/v1/videos?sort=-publishedAt",
            KIOSK_LOCAL, "%s/api/v1/videos?sort=-publishedAt&filter=local");

    private PeertubeTrendingLinkHandlerFactory() {
    }

    public static PeertubeTrendingLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public String getUrl(final String id,
                         final List<String> contentFilters,
                         final String sortFilter)
            throws ParsingException, UnsupportedOperationException {
        return getUrl(id, contentFilters, sortFilter, ServiceList.PeerTube.getBaseUrl());
    }

    @Override
    public String getUrl(final String id,
                         final List<String> contentFilters,
                         final String sortFilter,
                         final String baseUrl)
            throws ParsingException, UnsupportedOperationException {
        return String.format(KIOSK_MAP.get(id), baseUrl);
    }

    @Override
    public String getId(final String url) throws ParsingException, UnsupportedOperationException {
        final String cleanUrl = url.replace(ServiceList.PeerTube.getBaseUrl(), "%s");
        if (cleanUrl.contains("/videos/trending")) {
            return KIOSK_TRENDING;
        } else if (cleanUrl.contains("/videos/most-liked")) {
            return KIOSK_MOST_LIKED;
        } else if (cleanUrl.contains("/videos/recently-added")) {
            return KIOSK_RECENT;
        } else if (cleanUrl.contains("/videos/local")) {
            return KIOSK_LOCAL;
        } else {
            return KIOSK_MAP.entrySet().stream()
                    .filter(entry -> cleanUrl.equals(entry.getValue()))
                    .findFirst()
                    .map(Map.Entry::getKey)
                    .orElseThrow(() -> new ParsingException("no id found for this url"));
        }
    }

    @Override
    public boolean onAcceptUrl(final String url) {
        return url.contains("/videos?") || url.contains("/videos/trending")
                || url.contains("/videos/most-liked") || url.contains("/videos/recently-added")
                || url.contains("/videos/local");
    }
}
