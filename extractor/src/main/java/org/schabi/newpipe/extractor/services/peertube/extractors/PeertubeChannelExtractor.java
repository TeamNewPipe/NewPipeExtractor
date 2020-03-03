package org.schabi.newpipe.extractor.services.peertube.extractors;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.channel.ChannelTabExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.utils.JsonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PeertubeChannelExtractor extends ChannelExtractor {
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
        return null;
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
    public void onFetchPage(Downloader downloader) throws IOException, ExtractionException {
        Response response = downloader.get(getUrl());
        if (null != response && null != response.responseBody()) {
            setInitialData(response.responseBody());
        } else {
            throw new ExtractionException("Unable to extract PeerTube channel data");
        }
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
        return baseUrl + "/accounts/" + getId();
    }

    @Override
    public List<ChannelTabExtractor> getTabs() throws ParsingException {
        List<ChannelTabExtractor> tabs = new ArrayList<>();

        tabs.add(new PeertubeChannelVideosExtractor(getService(), (ListLinkHandler) getLinkHandler()));

        return tabs;
    }
}
