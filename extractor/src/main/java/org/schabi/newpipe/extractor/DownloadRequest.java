package org.schabi.newpipe.extractor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DownloadRequest {
    
    private final String requestBody;
    private final Map<String, List<String>> requestHeaders;
    public static final DownloadRequest emptyRequest = new DownloadRequest(null, null);

    public DownloadRequest(String requestBody, Map<String, List<String>> headers) {
        super();
        this.requestBody = requestBody;
        if(null != headers) {
            this.requestHeaders = headers;
        }else {
            this.requestHeaders = Collections.emptyMap();
        }
    }

    public String getRequestBody() {
        return requestBody;
    }

    public Map<String, List<String>> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestCookies(List<String> cookies){
        requestHeaders.put("Cookie", cookies);
    }
    
    public List<String> getRequestCookies(){
        if(null == requestHeaders) return Collections.emptyList();
        List<String> cookies = requestHeaders.get("Cookie");
        if(null == cookies)
            return Collections.emptyList();
        else
            return cookies;
    }

}
