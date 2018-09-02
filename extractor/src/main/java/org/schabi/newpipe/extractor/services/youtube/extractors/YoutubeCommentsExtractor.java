package org.schabi.newpipe.extractor.services.youtube.extractors;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class YoutubeCommentsExtractor extends CommentsExtractor {

    private static final String USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:61.0) Gecko/20100101 Firefox/61.0";

    private List<String> cookies;
    private String sessionToken;
    private String commentsToken;

    private ObjectMapper mapper = new ObjectMapper();

    public YoutubeCommentsExtractor(StreamingService service, ListLinkHandler uiHandler) {
        super(service, uiHandler);
        // TODO Auto-generated constructor stub
    }

    @Override
    public InfoItemsPage<CommentsInfoItem> getInitialPage() throws IOException, ExtractionException {
        // initial page does not load any comments but is required to get session token
        // and cookies
        super.fetchPage();
        return getPage(getNextPageUrl());
    }

    @Override
    public String getNextPageUrl() throws IOException, ExtractionException {
        return getNextPageUrl(commentsToken);
    }

    private String getNextPageUrl(JsonNode ajaxJson) throws IOException, ExtractionException {
        Optional<JsonNode> element = Optional.ofNullable(ajaxJson.findValue("itemSectionContinuation"))
                .map(e -> e.get("continuations")).map(e -> e.findValue("continuation"));

        if (element.isPresent()) {
            return getNextPageUrl(element.get().asText());
        } else {
            // no more comments
            return "";
        }
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
        JsonNode ajaxJson = mapper.readTree(ajaxResponse);
        CommentsInfoItemsCollector collector = new CommentsInfoItemsCollector(getServiceId());
        collectCommentsFrom(collector, ajaxJson, pageUrl);
        return new InfoItemsPage<>(collector, getNextPageUrl(ajaxJson));
    }

    private void collectCommentsFrom(CommentsInfoItemsCollector collector, JsonNode ajaxJson, String pageUrl) {
        List<JsonNode> comments = ajaxJson.findValues("commentRenderer");
        comments.stream().forEach(c -> {
            CommentsInfoItemExtractor extractor = new CommentsInfoItemExtractor() {

                @Override
                public String getUrl() throws ParsingException {
                    return pageUrl;
                }

                @Override
                public String getThumbnailUrl() throws ParsingException {
                    try {
                        return c.get("authorThumbnail").get("thumbnails").get(2).get("url").asText();
                    } catch (Exception e) {
                        throw new ParsingException("Could not get thumbnail url", e);
                    }
                }

                @Override
                public String getName() throws ParsingException {
                    try {
                        return c.get("authorText").get("simpleText").asText();
                    } catch (Exception e) {
                        throw new ParsingException("Could not get author name", e);
                    }
                }

                @Override
                public String getPublishedTime() throws ParsingException {
                    try {
                        return c.get("publishedTimeText").get("runs").get(0).get("text").asText();
                    } catch (Exception e) {
                        throw new ParsingException("Could not get publishedTimeText", e);
                    }
                }

                @Override
                public Integer getLikeCount() throws ParsingException {
                    try {
                        return c.get("likeCount").intValue();
                    } catch (Exception e) {
                        throw new ParsingException("Could not get like count", e);
                    }
                }

                @Override
                public String getCommentText() throws ParsingException {
                    try {
                        if (null != c.get("contentText").get("simpleText")) {
                            return c.get("contentText").get("simpleText").asText();
                        } else {
                            return c.get("contentText").get("runs").get(0).get("text").asText();
                        }
                    } catch (Exception e) {
                        throw new ParsingException("Could not get comment text", e);
                    }
                }

                @Override
                public String getCommentId() throws ParsingException {
                    try {
                        return c.get("commentId").asText();
                    } catch (Exception e) {
                        throw new ParsingException("Could not get comment id", e);
                    }
                }

                @Override
                public String getAuthorThumbnail() throws ParsingException {
                    try {
                        return c.get("authorThumbnail").get("thumbnails").get(2).get("url").asText();
                    } catch (Exception e) {
                        throw new ParsingException("Could not get author thumbnail", e);
                    }
                }

                @Override
                public String getAuthorName() throws ParsingException {
                    try {
                        return c.get("authorText").get("simpleText").asText();
                    } catch (Exception e) {
                        throw new ParsingException("Could not get author name", e);
                    }
                }

                @Override
                public String getAuthorEndpoint() throws ParsingException {
                    try {
                        return "https://youtube.com"
                                + c.get("authorEndpoint").get("browseEndpoint").get("canonicalBaseUrl").asText();
                    } catch (Exception e) {
                        throw new ParsingException("Could not get author endpoint", e);
                    }
                }
            };

            collector.commit(extractor);
        });

    }

    @Override
    public void onFetchPage(Downloader downloader) throws IOException, ExtractionException {
        DownloadResponse response = downloader.get(getUrl());
        String responseBody = response.getResponseBody();
        cookies = response.getResponseHeaders().get("Set-Cookie");
        sessionToken = findValue(responseBody, "XSRF_TOKEN");
        commentsToken = findValue(responseBody, "COMMENTS_TOKEN");
    }

    @Override
    public String getName() throws ParsingException {
        // TODO Auto-generated method stub
        return null;
    }

    private String makeAjaxRequest(String siteUrl) throws IOException, ReCaptchaException {

        StringBuilder postData = new StringBuilder();
        postData.append(URLEncoder.encode("session_token", "UTF-8"));
        postData.append('=');
        postData.append(URLEncoder.encode(sessionToken, "UTF-8"));

        Map<String, List<String>> requestHeaders = new HashMap<>();
        requestHeaders.put("Content-Type", Arrays.asList("application/x-www-form-urlencoded"));
        requestHeaders.put("Accept", Arrays.asList("*/*"));
        requestHeaders.put("User-Agent", Arrays.asList(USER_AGENT));
        requestHeaders.put("X-YouTube-Client-Version", Arrays.asList("2.20180815"));
        requestHeaders.put("X-YouTube-Client-Name", Arrays.asList("1"));
        requestHeaders.put("Cookie", cookies);

        return NewPipe.getDownloader().post(siteUrl, postData.toString(), requestHeaders).getResponseBody();
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
