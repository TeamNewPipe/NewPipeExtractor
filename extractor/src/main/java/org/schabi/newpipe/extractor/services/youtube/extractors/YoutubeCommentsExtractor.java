package org.schabi.newpipe.extractor.services.youtube.extractors;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.schabi.newpipe.extractor.DownloadRequest;
import org.schabi.newpipe.extractor.DownloadResponse;
import org.schabi.newpipe.extractor.Downloader;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.comments.CommentsExtractor;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.comments.CommentsInfoItemExtractor;
import org.schabi.newpipe.extractor.comments.CommentsInfoItemsCollector;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.utils.JsonUtils;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

public class YoutubeCommentsExtractor extends CommentsExtractor {

    private static final String USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:61.0) Gecko/20100101 Firefox/61.0";

    private List<String> cookies;
    private String sessionToken;
    private String title;
    private InfoItemsPage<CommentsInfoItem> initPage;

    public YoutubeCommentsExtractor(StreamingService service, ListLinkHandler uiHandler) {
        super(service, uiHandler);
    }

    @Override
    public InfoItemsPage<CommentsInfoItem> getInitialPage() throws IOException, ExtractionException {
        // initial page does not load any comments but is required to get session token
        // and cookies
        super.fetchPage();
        return initPage;
    }

    // isn't this method redundant. you can just call getnextpage on getInitialPage
    @Override
    public String getNextPageUrl() throws IOException, ExtractionException {
        // initial page does not load any comments but is required to get session token
        // and cookies
        super.fetchPage();
        return initPage.getNextPageUrl();
    }

    private String getNextPageUrl(JsonObject ajaxJson) throws IOException, ParsingException {
        
        JsonArray arr;
        try {
            arr = (JsonArray) JsonUtils.getValue(ajaxJson, "response.continuationContents.itemSectionContinuation.continuations");
        } catch (Exception e) {
            return "";
        }
        if(arr.isEmpty()) {
            return "";
        }
        String continuation;
        try {
            continuation = (String) JsonUtils.getValue(arr.getObject(0), "nextContinuationData.continuation");
        } catch (Exception e) {
            return "";
        }
        return getNextPageUrl(continuation);
    }

    private String getNextPageUrl(String continuation) throws ParsingException {
        Map<String, String> params = new HashMap<>();
        params.put("action_get_comments", "1");
        params.put("pbj", "1");
        params.put("ctoken", continuation);
        params.put("continuation", continuation);
        try {
            return "https://www.youtube.com/comment_service_ajax?" + getDataString(params);
        } catch (UnsupportedEncodingException e) {
            throw new ParsingException("Could not get next page url", e);
        }
    }

    @Override
    public InfoItemsPage<CommentsInfoItem> getPage(String pageUrl) throws IOException, ExtractionException {
        if (pageUrl == null || pageUrl.isEmpty()) {
            throw new ExtractionException(new IllegalArgumentException("Page url is empty or null"));
        }
        String ajaxResponse = makeAjaxRequest(pageUrl);
        JsonObject ajaxJson;
        try {
            ajaxJson = JsonParser.object().from(ajaxResponse);
        } catch (JsonParserException e) {
            throw new ParsingException("Could not parse json data for comments", e);
        }
        CommentsInfoItemsCollector collector = new CommentsInfoItemsCollector(getServiceId());
        collectCommentsFrom(collector, ajaxJson, pageUrl);
        return new InfoItemsPage<>(collector, getNextPageUrl(ajaxJson));
    }

    private void collectCommentsFrom(CommentsInfoItemsCollector collector, JsonObject ajaxJson, String pageUrl) throws ParsingException {
        
        JsonArray contents;
        try {
            contents = (JsonArray) JsonUtils.getValue(ajaxJson, "response.continuationContents.itemSectionContinuation.contents");
        }catch(Exception e) {
            throw new ParsingException("unable to get parse youtube comments", e);
        }
        fetchTitle(contents);
        List<Object> comments;
        try {
            comments = JsonUtils.getValues(contents, "commentThreadRenderer.comment.commentRenderer");
        }catch(Exception e) {
            throw new ParsingException("unable to get parse youtube comments", e);
        }
        
        for(Object c: comments) {
            if(c instanceof JsonObject) {
                CommentsInfoItemExtractor extractor = new YoutubeCommentsInfoItemExtractor((JsonObject) c, pageUrl);
                collector.commit(extractor);
            }
        }
    }

    private void fetchTitle(JsonArray contents) {
        if(null == title) {
            try {
                title = (String) JsonUtils.getValue(contents.getObject(0), "commentThreadRenderer.commentTargetTitle.simpleText");
            } catch (Exception e) {
                title = "Youtube Comments";
            }
        }
    }

    @Override
    public void onFetchPage(Downloader downloader) throws IOException, ExtractionException {
        DownloadResponse response = downloader.get(getUrl());
        String responseBody = response.getResponseBody();
        cookies = response.getResponseCookies();
        sessionToken = findValue(responseBody, "XSRF_TOKEN");
        String commentsToken = findValue(responseBody, "COMMENTS_TOKEN");
        initPage = getPage(getNextPageUrl(commentsToken));
    }

    @Override
    public String getName() throws ParsingException {
        return title;
    }

    private String makeAjaxRequest(String siteUrl) throws IOException, ReCaptchaException {

        Map<String, String> postDataMap = new HashMap<>();
        postDataMap.put("session_token", sessionToken);
        String postData = getDataString(postDataMap);

        Map<String, List<String>> requestHeaders = new HashMap<>();
        requestHeaders.put("Content-Type", Arrays.asList("application/x-www-form-urlencoded"));
        requestHeaders.put("Accept", Arrays.asList("*/*"));
        requestHeaders.put("User-Agent", Arrays.asList(USER_AGENT));
        requestHeaders.put("X-YouTube-Client-Version", Arrays.asList("2.20180815"));
        requestHeaders.put("X-YouTube-Client-Name", Arrays.asList("1"));
        DownloadRequest request = new DownloadRequest(postData, requestHeaders);
        request.setRequestCookies(cookies);

        return NewPipe.getDownloader().post(siteUrl, request).getResponseBody();
    }

    private String getDataString(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }

    private String findValue(String doc, String key) {
        int beginIndex = doc.indexOf(key) + key.length() + 4;
        int endIndex = doc.indexOf("\"", beginIndex);
        return doc.substring(beginIndex, endIndex);
    }

}
