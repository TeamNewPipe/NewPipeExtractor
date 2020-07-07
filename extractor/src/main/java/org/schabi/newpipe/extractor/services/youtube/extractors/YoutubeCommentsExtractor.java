package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;

import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.comments.CommentsExtractor;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.comments.CommentsInfoItemExtractor;
import org.schabi.newpipe.extractor.comments.CommentsInfoItemsCollector;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Parser;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import static java.util.Collections.singletonList;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public class YoutubeCommentsExtractor extends CommentsExtractor {
    // using the mobile site for comments because it loads faster and uses get requests instead of post
    private static final String USER_AGENT = "Mozilla/5.0 (Android 8.1.0; Mobile; rv:62.0) Gecko/62.0 Firefox/62.0";
    private static final Pattern YT_CLIENT_NAME_PATTERN = Pattern.compile("INNERTUBE_CONTEXT_CLIENT_NAME\\\":(.*?)[,}]");

    private String ytClientVersion;
    private String ytClientName;
    private String responseBody;

    public YoutubeCommentsExtractor(StreamingService service, ListLinkHandler uiHandler) {
        super(service, uiHandler);
    }

    @Override
    public InfoItemsPage<CommentsInfoItem> getInitialPage() throws IOException, ExtractionException {
        String commentsTokenInside = findValue(responseBody, "commentSectionRenderer", "}");
        String commentsToken = findValue(commentsTokenInside, "continuation\":\"", "\"");
        return getPage(getNextPage(commentsToken));
    }

    private Page getNextPage(JsonObject ajaxJson) throws ParsingException {
        JsonArray arr;
        try {
            arr = JsonUtils.getArray(ajaxJson, "response.continuationContents.commentSectionContinuation.continuations");
        } catch (Exception e) {
            return null;
        }
        if (arr.isEmpty()) {
            return null;
        }
        String continuation;
        try {
            continuation = JsonUtils.getString(arr.getObject(0), "nextContinuationData.continuation");
        } catch (Exception e) {
            return null;
        }
        return getNextPage(continuation);
    }

    private Page getNextPage(String continuation) throws ParsingException {
        Map<String, String> params = new HashMap<>();
        params.put("action_get_comments", "1");
        params.put("pbj", "1");
        params.put("ctoken", continuation);
        try {
            return new Page("https://m.youtube.com/watch_comment?" + getDataString(params));
        } catch (UnsupportedEncodingException e) {
            throw new ParsingException("Could not get next page url", e);
        }
    }

    @Override
    public InfoItemsPage<CommentsInfoItem> getPage(final Page page) throws IOException, ExtractionException {
        if (page == null || isNullOrEmpty(page.getUrl())) {
            throw new IllegalArgumentException("Page doesn't contain an URL");
        }

        String ajaxResponse = makeAjaxRequest(page.getUrl());
        JsonObject ajaxJson;
        try {
            ajaxJson = JsonParser.array().from(ajaxResponse).getObject(1);
        } catch (Exception e) {
            throw new ParsingException("Could not parse json data for comments", e);
        }
        CommentsInfoItemsCollector collector = new CommentsInfoItemsCollector(getServiceId());
        collectCommentsFrom(collector, ajaxJson);
        return new InfoItemsPage<>(collector, getNextPage(ajaxJson));
    }

    private void collectCommentsFrom(CommentsInfoItemsCollector collector, JsonObject ajaxJson) throws ParsingException {
        JsonArray contents;
        try {
            contents = JsonUtils.getArray(ajaxJson, "response.continuationContents.commentSectionContinuation.items");
        } catch (Exception e) {
            //no comments
            return;
        }
        List<Object> comments;
        try {
            comments = JsonUtils.getValues(contents, "commentThreadRenderer.comment.commentRenderer");
        } catch (Exception e) {
            throw new ParsingException("unable to get parse youtube comments", e);
        }

        for (Object c : comments) {
            if (c instanceof JsonObject) {
                CommentsInfoItemExtractor extractor = new YoutubeCommentsInfoItemExtractor((JsonObject) c, getUrl(), getTimeAgoParser());
                collector.commit(extractor);
            }
        }
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        final Map<String, List<String>> requestHeaders = new HashMap<>();
        requestHeaders.put("User-Agent", singletonList(USER_AGENT));
        final Response response = downloader.get(getUrl(), requestHeaders, getExtractorLocalization());
        responseBody = response.responseBody();
        ytClientVersion = findValue(responseBody, "INNERTUBE_CONTEXT_CLIENT_VERSION\":\"", "\"");
        ytClientName = Parser.matchGroup1(YT_CLIENT_NAME_PATTERN, responseBody);
    }


    private String makeAjaxRequest(String siteUrl) throws IOException, ReCaptchaException {
        Map<String, List<String>> requestHeaders = new HashMap<>();
        requestHeaders.put("Accept", singletonList("*/*"));
        requestHeaders.put("User-Agent", singletonList(USER_AGENT));
        requestHeaders.put("X-YouTube-Client-Version", singletonList(ytClientVersion));
        requestHeaders.put("X-YouTube-Client-Name", singletonList(ytClientName));
        return getDownloader().get(siteUrl, requestHeaders, getExtractorLocalization()).responseBody();
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
}
