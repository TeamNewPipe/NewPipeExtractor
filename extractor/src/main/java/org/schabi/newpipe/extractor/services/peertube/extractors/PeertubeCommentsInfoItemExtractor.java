package org.schabi.newpipe.extractor.services.peertube.extractors;

import com.grack.nanojson.JsonObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.comments.CommentsInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper;
import org.schabi.newpipe.extractor.utils.JsonUtils;

import java.util.Objects;

public class PeertubeCommentsInfoItemExtractor implements CommentsInfoItemExtractor {
    private final JsonObject item;
    private final String url;
    private final String baseUrl;

    public PeertubeCommentsInfoItemExtractor(final JsonObject item, final PeertubeCommentsExtractor extractor) throws ParsingException {
        this.item = item;
        this.url = extractor.getUrl();
        this.baseUrl = extractor.getBaseUrl();
    }

    @Override
    public String getUrl() throws ParsingException {
        return url;
    }

    @Override
    public String getThumbnailUrl() {
        String value;
        try {
            value = JsonUtils.getString(item, "account.avatar.path");
        } catch (Exception e) {
            value = "/client/assets/images/default-avatar.png";
        }
        return baseUrl + value;
    }

    @Override
    public String getName() throws ParsingException {
        return JsonUtils.getString(item, "account.displayName");
    }

    @Override
    public String getTextualUploadDate() throws ParsingException {
        return JsonUtils.getString(item, "createdAt");
    }

    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        final String textualUploadDate = getTextualUploadDate();
        return new DateWrapper(PeertubeParsingHelper.parseDateFrom(textualUploadDate));
    }

    @Override
    public int getLikeCount() {
        return -1;
    }

    @Override
    public String getCommentText() throws ParsingException {
        final String htmlText = JsonUtils.getString(item, "text");
        try {
            final Document doc = Jsoup.parse(htmlText);
            return doc.body().text();
        } catch (Exception e) {
            return htmlText.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", "");
        }
    }

    @Override
    public String getCommentId() {
        return Objects.toString(item.getLong("id"), null);
    }

    @Override
    public String getUploaderAvatarUrl() {
        String value;
        try {
            value = JsonUtils.getString(item, "account.avatar.path");
        } catch (Exception e) {
            value = "/client/assets/images/default-avatar.png";
        }
        return baseUrl + value;
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return JsonUtils.getString(item, "account.name") + "@" + JsonUtils.getString(item, "account.host");
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        final String name = JsonUtils.getString(item, "account.name");
        final String host = JsonUtils.getString(item, "account.host");
        return ServiceList.PeerTube.getChannelLHFactory().fromId("accounts/" + name + "@" + host, baseUrl).getUrl();
    }
}
