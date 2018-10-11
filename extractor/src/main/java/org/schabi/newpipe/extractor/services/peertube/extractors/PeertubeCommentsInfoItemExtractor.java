package org.schabi.newpipe.extractor.services.peertube.extractors;

import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.comments.CommentsInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeChannelLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.JsonUtils;

import com.grack.nanojson.JsonObject;


public class PeertubeCommentsInfoItemExtractor implements CommentsInfoItemExtractor {

    private final JsonObject item;
    private final String url;

    public PeertubeCommentsInfoItemExtractor(JsonObject item, String url) {
        this.item = item;
        this.url = url;
    }

    @Override
    public String getUrl() throws ParsingException {
        return url;
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        String value;
        try {
            value = JsonUtils.getString(item, "account.avatar.path");
        }catch(Exception e) {
            value = "/client/assets/images/default-avatar.png";
        }
        return ServiceList.PeerTube.getBaseUrl() + value;
    }

    @Override
    public String getName() throws ParsingException {
        return JsonUtils.getString(item, "account.displayName");
    }

    @Override
    public String getPublishedTime() throws ParsingException {
        return JsonUtils.getString(item, "createdAt");
    }

    @Override
    public Integer getLikeCount() throws ParsingException {
        return 0;
    }

    @Override
    public String getCommentText() throws ParsingException {
        String htmlText = JsonUtils.getString(item, "text");
        return htmlText.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", "");
    }

    @Override
    public String getCommentId() throws ParsingException {
        Number value = JsonUtils.getNumber(item, "id");
        return value.toString();
    }

    @Override
    public String getAuthorThumbnail() throws ParsingException {
        String value;
        try {
            value = JsonUtils.getString(item, "account.avatar.path");
        }catch(Exception e) {
            value = "/client/assets/images/default-avatar.png";
        }
        return ServiceList.PeerTube.getBaseUrl() + value;
    }

    @Override
    public String getAuthorName() throws ParsingException {
        return JsonUtils.getString(item, "account.displayName");
    }

    @Override
    public String getAuthorEndpoint() throws ParsingException {
        String name = JsonUtils.getString(item, "account.name");
        String host = JsonUtils.getString(item, "account.host");
        return PeertubeChannelLinkHandlerFactory.getInstance().fromId(name + "@" + host).getUrl();
    }

}
