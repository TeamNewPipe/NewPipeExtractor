package org.schabi.newpipe.extractor.services.peertube.extractors;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
        String date = JsonUtils.getString(item, "createdAt");
        return getFormattedDate(date);
    }

    @Override
    public Integer getLikeCount() throws ParsingException {
        return 0;
    }

    @Override
    public String getCommentText() throws ParsingException {
        String htmlText = JsonUtils.getString(item, "text");
        try {
            Document doc = Jsoup.parse(htmlText);
            return doc.body().text();
        }catch(Exception e) {
            return htmlText.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", "");
        }
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
    
    private String getFormattedDate(String date) {
        DateFormat sourceDf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        DateFormat targetDf = new SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.ENGLISH);
        try {
            return targetDf.format(sourceDf.parse(date));
        } catch (ParseException e) {
            return date;
        }
    }

}
