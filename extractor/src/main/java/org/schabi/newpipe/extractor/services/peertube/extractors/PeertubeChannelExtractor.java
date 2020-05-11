package org.schabi.newpipe.extractor.services.peertube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.jsoup.helper.StringUtil;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.Parser.RegexException;

import java.io.IOException;

import static org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper.*;

public class PeertubeChannelExtractor extends ChannelExtractor {

    private InfoItemsPage<StreamInfoItem> initPage;
    private long total;

    private JsonObject json;
    private final String baseUrl;

    public PeertubeChannelExtractor(StreamingService service, ListLinkHandler linkHandler) throws ParsingException {
        super(service, linkHandler);
        this.baseUrl = getBaseUrl();
    }

    @Override
    public String getAvatarUrl() throws ParsingException {
        String value;
        try {
            value = JsonUtils.getString(json, "avatar.path");
        } catch (Exception e) {
            value = "/client/assets/images/default-avatar.png";
        }
        return baseUrl + value;
    }

    @Override
    public String getBannerUrl() throws ParsingException {
        return null;
    }

    @Override
    public String getFeedUrl() throws ParsingException {
        return getBaseUrl() + "/feeds/videos.xml?videoChannelId=" + json.get("id");
    }

    @Override
    public long getSubscriberCount() throws ParsingException {
        Number number = JsonUtils.getNumber(json, "followersCount");
        return number.longValue();
    }

    @Override
    public String getDescription() throws ParsingException {
        try {
            return JsonUtils.getString(json, "description");
        } catch (ParsingException e) {
            return "No description";
        }
    }

    @Override
    public String getParentChannelName() throws ParsingException {
        return JsonUtils.getString(json, "ownerAccount.name");
    }

    @Override
    public String getParentChannelUrl() throws ParsingException {
        return JsonUtils.getString(json, "ownerAccount.url");
    }

    @Override
    public String getParentChannelAvatarUrl() throws ParsingException {
        String value;
        try {
            value = JsonUtils.getString(json, "ownerAccount.avatar.path");
        } catch (Exception e) {
            value = "/client/assets/images/default-avatar.png";
        }
        return baseUrl + value;
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws IOException, ExtractionException {
        super.fetchPage();
        return initPage;
    }

    private void collectStreamsFrom(StreamInfoItemsCollector collector, JsonObject json, String pageUrl) throws ParsingException {
        JsonArray contents;
        try {
            contents = (JsonArray) JsonUtils.getValue(json, "data");
        } catch (Exception e) {
            throw new ParsingException("unable to extract channel streams", e);
        }

        for (Object c : contents) {
            if (c instanceof JsonObject) {
                final JsonObject item = (JsonObject) c;
                PeertubeStreamInfoItemExtractor extractor = new PeertubeStreamInfoItemExtractor(item, baseUrl);
                collector.commit(extractor);
            }
        }

    }

    @Override
    public String getNextPageUrl() throws IOException, ExtractionException {
        super.fetchPage();
        return initPage.getNextPageUrl();
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(String pageUrl) throws IOException, ExtractionException {
        Response response = getDownloader().get(pageUrl);
        JsonObject json = null;
        if (null != response && !StringUtil.isBlank(response.responseBody())) {
            try {
                json = JsonParser.object().from(response.responseBody());
            } catch (Exception e) {
                throw new ParsingException("Could not parse json data for kiosk info", e);
            }
        }

        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        if (json != null) {
            PeertubeParsingHelper.validate(json);
            this.total = JsonUtils.getNumber(json, "total").longValue();
            collectStreamsFrom(collector, json, pageUrl);
        } else {
            throw new ExtractionException("Unable to get PeerTube kiosk info");
        }
        return new InfoItemsPage<>(collector, PeertubeParsingHelper.getNextPageUrl(pageUrl, total));
    }

    @Override
    public void onFetchPage(Downloader downloader) throws IOException, ExtractionException {
        Response response = downloader.get(getUrl());
        if (null != response && null != response.responseBody()) {
            setInitialData(response.responseBody());
        } else {
            throw new ExtractionException("Unable to extract PeerTube channel data");
        }

        this.initPage = getPage(getUrl() + "/videos?" + START_KEY + "=0&" + COUNT_KEY + "=" + ITEMS_PER_PAGE);
    }

    private void setInitialData(String responseBody) throws ExtractionException {
        try {
            json = JsonParser.object().from(responseBody);
        } catch (JsonParserException e) {
            throw new ExtractionException("Unable to extract peertube channel data", e);
        }
        if (json == null) throw new ExtractionException("Unable to extract PeerTube channel data");
    }

    @Override
    public String getName() throws ParsingException {
        return JsonUtils.getString(json, "displayName");
    }

    @Override
    public String getOriginalUrl() throws ParsingException {
        return baseUrl + "/" + getId();
    }

}
