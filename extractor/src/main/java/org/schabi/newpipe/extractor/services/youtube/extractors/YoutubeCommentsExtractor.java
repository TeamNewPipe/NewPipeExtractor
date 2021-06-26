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
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Parser;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.Collections.singletonList;
import static org.schabi.newpipe.extractor.utils.Utils.UTF_8;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public class YoutubeCommentsExtractor extends CommentsExtractor {
    // using the mobile site for comments because it loads faster and uses get requests instead of post
    private static final String USER_AGENT = "Mozilla/5.0 (Android 9; Mobile; rv:78.0) Gecko/20100101 Firefox/78.0";
    private static final Pattern YT_CLIENT_NAME_PATTERN = Pattern.compile("INNERTUBE_CONTEXT_CLIENT_NAME\\\":(.*?)[,}]");

    private String ytClientVersion;
    private String ytClientName;
    private String responseBody;

    /**
     * Caching mechanism and holder of the commentsDisabled value.
     * <br/>
     * Initial value = empty -> unknown if comments are disabled or not<br/>
     * Some method calls {@link YoutubeCommentsExtractor#findInitialCommentsToken()}
     * -> value is set<br/>
     * If the method or another one that is depending on disabled comments
     * is now called again, the method execution can avoid unnecessary calls
     */
    private Optional<Boolean> optCommentsDisabled = Optional.empty();

    public YoutubeCommentsExtractor(
            final StreamingService service,
            final ListLinkHandler uiHandler) {
        super(service, uiHandler);
    }

    @Override
    public InfoItemsPage<CommentsInfoItem> getInitialPage()
            throws IOException, ExtractionException {

        // Check if findInitialCommentsToken was already called and optCommentsDisabled initialized
        if (optCommentsDisabled.orElse(false)) {
            return getInfoItemsPageForDisabledComments();
        }

        // Get the token
        final String commentsToken = findInitialCommentsToken();
        // Check if the comments have been disabled
        if (optCommentsDisabled.get()) {
            return getInfoItemsPageForDisabledComments();
        }

        return getPage(getNextPage(commentsToken));
    }

    /**
     * Finds the initial comments token and initializes commentsDisabled.
     * @return the continuation token or null if none was found
     */
    private String findInitialCommentsToken() {
        final String continuationStartPattern = "continuation\":\"";

        String commentsTokenInside = findValue(responseBody, "sectionListRenderer", "}");
        if (commentsTokenInside == null || !commentsTokenInside.contains(continuationStartPattern)) {
            commentsTokenInside = findValue(responseBody, "commentSectionRenderer", "}");
        }

        // If no continuation token is found the comments are disabled
        if (commentsTokenInside == null || !commentsTokenInside.contains(continuationStartPattern)) {
            optCommentsDisabled = Optional.of(true);
            return null;
        }

        // If a continuation token is found there are >= 0 comments
        final String commentsToken = findValue(commentsTokenInside, continuationStartPattern, "\"");

        optCommentsDisabled = Optional.of(false);

        return commentsToken;
    }

    private InfoItemsPage<CommentsInfoItem> getInfoItemsPageForDisabledComments() {
        return new InfoItemsPage<>(Collections.emptyList(), null, Collections.emptyList());
    }

    private Page getNextPage(final JsonObject ajaxJson) throws ParsingException {
        final JsonArray arr;
        try {
            arr = JsonUtils.getArray(ajaxJson, "response.continuationContents.commentSectionContinuation.continuations");
        } catch (final Exception e) {
            return null;
        }
        if (arr.isEmpty()) {
            return null;
        }
        final String continuation;
        try {
            continuation = JsonUtils.getString(arr.getObject(0), "nextContinuationData.continuation");
        } catch (final Exception e) {
            return null;
        }
        return getNextPage(continuation);
    }

    private Page getNextPage(final String continuation) throws ParsingException {
        final Map<String, String> params = new HashMap<>();
        params.put("action_get_comments", "1");
        params.put("pbj", "1");
        params.put("ctoken", continuation);
        try {
            return new Page("https://m.youtube.com/watch_comment?" + getDataString(params));
        } catch (final UnsupportedEncodingException e) {
            throw new ParsingException("Could not get next page url", e);
        }
    }

    @Override
    public InfoItemsPage<CommentsInfoItem> getPage(final Page page) throws IOException, ExtractionException {
        if (optCommentsDisabled.orElse(false)) {
            return getInfoItemsPageForDisabledComments();
        }
        if (page == null || isNullOrEmpty(page.getUrl())) {
            throw new IllegalArgumentException("Page doesn't contain an URL");
        }

        final String ajaxResponse = makeAjaxRequest(page.getUrl());
        final JsonObject ajaxJson;
        try {
            ajaxJson = JsonParser.array().from(ajaxResponse).getObject(1);
        } catch (final Exception e) {
            throw new ParsingException("Could not parse json data for comments", e);
        }
        final CommentsInfoItemsCollector collector = new CommentsInfoItemsCollector(getServiceId());
        collectCommentsFrom(collector, ajaxJson);
        return new InfoItemsPage<>(collector, getNextPage(ajaxJson));
    }

    private void collectCommentsFrom(final CommentsInfoItemsCollector collector, final JsonObject ajaxJson) throws ParsingException {
        final JsonArray contents;
        try {
            contents = JsonUtils.getArray(ajaxJson, "response.continuationContents.commentSectionContinuation.items");
        } catch (final Exception e) {
            //no comments
            return;
        }
        final List<Object> comments;
        try {
            comments = JsonUtils.getValues(contents, "commentThreadRenderer.comment.commentRenderer");
        } catch (final Exception e) {
            throw new ParsingException("unable to get parse youtube comments", e);
        }

        for (final Object c : comments) {
            if (c instanceof JsonObject) {
                final CommentsInfoItemExtractor extractor =
                        new YoutubeCommentsInfoItemExtractor((JsonObject) c, getUrl(), getTimeAgoParser());
                collector.commit(extractor);
            }
        }
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) throws IOException, ExtractionException {
        final Map<String, List<String>> requestHeaders = new HashMap<>();
        requestHeaders.put("User-Agent", singletonList(USER_AGENT));
        final Response response = downloader.get(getUrl() + "&ucbcb=1", requestHeaders, getExtractorLocalization());
        responseBody = YoutubeParsingHelper.unescapeDocument(response.responseBody());
        ytClientVersion = findValue(responseBody, "INNERTUBE_CONTEXT_CLIENT_VERSION\":\"", "\"");
        ytClientName = Parser.matchGroup1(YT_CLIENT_NAME_PATTERN, responseBody);
    }


    private String makeAjaxRequest(final String siteUrl) throws IOException, ReCaptchaException {
        final Map<String, List<String>> requestHeaders = new HashMap<>();
        requestHeaders.put("Accept", singletonList("*/*"));
        requestHeaders.put("User-Agent", singletonList(USER_AGENT));
        requestHeaders.put("X-YouTube-Client-Version", singletonList(ytClientVersion));
        requestHeaders.put("X-YouTube-Client-Name", singletonList(ytClientName));
        return getDownloader().get(siteUrl, requestHeaders, getExtractorLocalization()).responseBody();
    }

    private String getDataString(final Map<String, String> params) throws UnsupportedEncodingException {
        final StringBuilder result = new StringBuilder();
        boolean first = true;
        for (final Map.Entry<String, String> entry : params.entrySet()) {
            if (first) {
                first = false;
            } else {
                result.append("&");
            }
            result.append(URLEncoder.encode(entry.getKey(), UTF_8));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), UTF_8));
        }
        return result.toString();
    }

    private String findValue(final String doc, final String start, final String end) {
        int beginIndex = doc.indexOf(start);
        // Start string was not found
        if (beginIndex == -1) {
            return null;
        }
        beginIndex = beginIndex + start.length();
        final int endIndex = doc.indexOf(end, beginIndex);
        // End string was not found
        if (endIndex == -1) {
            return null;
        }
        return doc.substring(beginIndex, endIndex);
    }

    @Override
    public boolean isCommentsDisabled() {
        // Check if commentsDisabled has to be initialized
        if (!optCommentsDisabled.isPresent()) {
            // Initialize commentsDisabled
            this.findInitialCommentsToken();
        }

        return optCommentsDisabled.get();
    }
}
