package org.schabi.newpipe.extractor.services.peertube.linkHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;

public class PeertubeTrendingLinkHandlerFactory extends ListLinkHandlerFactory {
    
    
    public static final Map<String, String> KIOSK_MAP;
    public static final String KIOSK_TRENDING = "Trending";
    public static final String KIOSK_RECENT = "Recently added";
    public static final String KIOSK_LOCAL = "Local";
    
    static {
        Map<String, String> map = new HashMap<>();
        map.put(KIOSK_TRENDING, "%s/api/v1/videos?sort=-views");
        map.put(KIOSK_RECENT, "%s/api/v1/videos?sort=-publishedAt");
        map.put(KIOSK_LOCAL, "%s/api/v1/videos?filter=local");
        KIOSK_MAP = Collections.unmodifiableMap(map);
    }
    
    public String getUrl(String id, List<String> contentFilters, String sortFilter) {
        String baseUrl = ServiceList.PeerTube.getBaseUrl();
        return String.format(KIOSK_MAP.get(id), baseUrl);
    }

    @Override
    public String getId(String url) throws ParsingException {
        
        if(url.contains("/videos/trending")) {
            return KIOSK_TRENDING;
        }else if(url.contains("/videos/recently-added")) {
            return KIOSK_RECENT;
        }else if(url.contains("/videos/local")){
            return KIOSK_LOCAL;
        }else {
            throw new ParsingException("no id found for this url");
        }
    }

    @Override
    public boolean onAcceptUrl(final String url) {
        return url.contains("/videos/trending") || url.contains("/videos/recently-added") || url.contains("/videos/local");
    }
}
