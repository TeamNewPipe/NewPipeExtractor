package org.schabi.newpipe.extractor.services.youtube.extractors;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

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
import org.schabi.newpipe.extractor.utils.Localization;
import org.schabi.newpipe.extractor.utils.Parser;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;


public class YoutubeCommentsExtractor extends CommentsExtractor {

    // using the mobile site for comments because it loads faster and uses get requests instead of post
    private static final String USER_AGENT = "Mozilla/5.0 (Android 8.1.0; Mobile; rv:62.0) Gecko/62.0 Firefox/62.0";
    private static final Pattern YT_CLIENT_NAME_PATTERN = Pattern.compile("INNERTUBE_CONTEXT_CLIENT_NAME\\\":(.*?)[,}]");

    private String ytClientVersion;
    private String ytClientName;
    private String title;
    private InfoItemsPage<CommentsInfoItem> initPage;

    public YoutubeCommentsExtractor(StreamingService service, ListLinkHandler uiHandler, Localization localization) {
        super(service, uiHandler, localization);
    }

    @Override
    public InfoItemsPage<CommentsInfoItem> getInitialPage() throws IOException, ExtractionException {
        // initial page does not load any comments but is required to get comments token
        super.fetchPage();
        return initPage;
    }

    @Override
    public String getNextPageUrl() throws IOException, ExtractionException {
        // initial page does not load any comments but is required to get comments token
        super.fetchPage();
        return initPage.getNextPageUrl();
    }

    private String getNextPageUrl(JsonObject ajaxJson) throws IOException, ParsingException {
        
        JsonArray arr;
        try {
            arr = JsonUtils.getArray(ajaxJson, "response.continuationContents.commentSectionContinuation.continuations");
        } catch (Exception e) {
            return "";
        }
        if(arr.isEmpty()) {
            return "";
        }
        String continuation;
        try {
            continuation = JsonUtils.getString(arr.getObject(0), "nextContinuationData.continuation");
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
        try {
            return "https://m.youtube.com/watch_comment?" + getDataString(params);
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
            ajaxJson = JsonParser.array().from(ajaxResponse).getObject(1);
        } catch (Exception e) {
            throw new ParsingException("Could not parse json data for comments", e);
        }
        CommentsInfoItemsCollector collector = new CommentsInfoItemsCollector(getServiceId());
        collectCommentsFrom(collector, ajaxJson);
        return new InfoItemsPage<>(collector, getNextPageUrl(ajaxJson));
    }

    private void collectCommentsFrom(CommentsInfoItemsCollector collector, JsonObject ajaxJson) throws ParsingException {
        
        JsonArray contents;
        try {
            contents = JsonUtils.getArray(ajaxJson, "response.continuationContents.commentSectionContinuation.items");
        }catch(Exception e) {
            //no comments
            return;
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
                CommentsInfoItemExtractor extractor = new YoutubeCommentsInfoItemExtractor((JsonObject) c, getUrl());
                collector.commit(extractor);
            }
        }
    }

    private void fetchTitle(JsonArray contents) {
        if(null == title) {
            try {
                title = getYoutubeText(JsonUtils.getObject(contents.getObject(0), "commentThreadRenderer.commentTargetTitle"));
            } catch (Exception e) {
                title = "Youtube Comments";
            }
        }
    }

    @Override
    public void onFetchPage(Downloader downloader) throws IOException, ExtractionException {
        Map<String, List<String>> requestHeaders = new HashMap<>();
        requestHeaders.put("User-Agent", Arrays.asList(USER_AGENT));
        DownloadRequest request = new DownloadRequest(null, requestHeaders);
        DownloadResponse response = downloader.get(getUrl(), request);
        String responseBody = response.getResponseBody();
        ytClientVersion = findValue(responseBody, "INNERTUBE_CONTEXT_CLIENT_VERSION\":\"", "\"");
        ytClientName = Parser.matchGroup1(YT_CLIENT_NAME_PATTERN, responseBody);
        String commentsTokenInside = findValue(responseBody, "commentSectionRenderer", "}");
        String commentsToken = findValue(commentsTokenInside, "continuation\":\"", "\"");
        initPage = getPage(getNextPageUrl(commentsToken));
    }

    @Override
    public String getName() throws ParsingException {
        return title;
    }

    private String makeAjaxRequest(String siteUrl) throws IOException, ReCaptchaException {

        Map<String, List<String>> requestHeaders = new HashMap<>();
        requestHeaders.put("Accept", Arrays.asList("*/*"));
        requestHeaders.put("User-Agent", Arrays.asList(USER_AGENT));
        requestHeaders.put("X-YouTube-Client-Version", Arrays.asList(ytClientVersion));
        requestHeaders.put("X-YouTube-Client-Name", Arrays.asList(ytClientName));
        DownloadRequest request = new DownloadRequest(null, requestHeaders);

        return NewPipe.getDownloader().get(siteUrl, request).getResponseBody();
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

    private String findValue(String doc, String start, String end) {
        int beginIndex = doc.indexOf(start) + start.length();
        int endIndex = doc.indexOf(end, beginIndex);
        return doc.substring(beginIndex, endIndex);
    }
    
    public static String getYoutubeText(@Nonnull JsonObject object) throws ParsingException {
        try {
            return JsonUtils.getString(object, "simpleText");
        } catch (Exception e1) {
            try {
                JsonArray arr = JsonUtils.getArray(object, "runs");
                String result = "";
                for(int i=0; i<arr.size();i++) {
                    result = result + JsonUtils.getString(arr.getObject(i), "text");
                }
                return result;
            } catch (Exception e2) {
                return "";
            }
        }
    }
    
}
