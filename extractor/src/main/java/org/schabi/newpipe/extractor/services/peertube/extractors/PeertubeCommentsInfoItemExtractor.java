package org.schabi.newpipe.extractor.services.peertube.extractors;

import com.grack.nanojson.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.comments.CommentsInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper;
import org.schabi.newpipe.extractor.utils.JsonUtils;

import java.util.ArrayList;
import java.util.List;


public class PeertubeCommentsInfoItemExtractor implements CommentsInfoItemExtractor {

    private final JsonObject item;
    private final String url;
    private final String baseUrl;

    public PeertubeCommentsInfoItemExtractor(JsonObject item, PeertubeCommentsExtractor extractor) throws ParsingException {
        this.item = item;
        this.url = extractor.getUrl();
        this.baseUrl = extractor.getBaseUrl();
    }

    @Override
    public String getUrl() throws ParsingException {
        return url;
    }

    @Override
    public List<Image> getThumbnails() throws ParsingException {
        List<Image> images = new ArrayList<>();

        String value;
        try {
            value = JsonUtils.getString(item, "account.avatar.path");
        } catch (Exception e) {
            value = "/client/assets/images/default-avatar.png";
        }

        images.add(new Image(baseUrl + value, 120, 120)); // See https://github.com/Chocobozzz/PeerTube/blob/366caf8b71f3d82336b6ac243845c783ef673fc1/server/initializers/constants.ts#L558
        return images;
    }

    @Override
    public String getName() throws ParsingException {
        return JsonUtils.getString(item, "account.displayName");
    }

    @Override
    public String getTextualPublishedTime() throws ParsingException {
        return JsonUtils.getString(item, "createdAt");
    }

    @Override
    public DateWrapper getPublishedTime() throws ParsingException {
        String textualUploadDate = getTextualPublishedTime();
        return new DateWrapper(PeertubeParsingHelper.parseDateFrom(textualUploadDate));
    }

    @Override
    public int getLikeCount() throws ParsingException {
        return -1;
    }

    @Override
    public String getCommentText() throws ParsingException {
        String htmlText = JsonUtils.getString(item, "text");
        try {
            Document doc = Jsoup.parse(htmlText);
            return doc.body().text();
        } catch (Exception e) {
            return htmlText.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", "");
        }
    }

    @Override
    public String getCommentId() throws ParsingException {
        Number value = JsonUtils.getNumber(item, "id");
        return value.toString();
    }

    @Override
    public List<Image> getAuthorThumbnails() throws ParsingException {
        List<Image> images = new ArrayList<>();

        String value;
        try {
            value = JsonUtils.getString(item, "account.avatar.path");
        } catch (Exception e) {
            value = "/client/assets/images/default-avatar.png";
        }

        images.add(new Image(baseUrl + value, 120, 120)); // See https://github.com/Chocobozzz/PeerTube/blob/366caf8b71f3d82336b6ac243845c783ef673fc1/server/initializers/constants.ts#L558
        return images;
    }

    @Override
    public String getAuthorName() throws ParsingException {
        return JsonUtils.getString(item, "account.name") + "@" + JsonUtils.getString(item, "account.host");
    }

    @Override
    public String getAuthorEndpoint() throws ParsingException {
        String name = JsonUtils.getString(item, "account.name");
        String host = JsonUtils.getString(item, "account.host");
        return ServiceList.PeerTube.getChannelLHFactory().fromId(name + "@" + host, baseUrl).getUrl();
    }

}
