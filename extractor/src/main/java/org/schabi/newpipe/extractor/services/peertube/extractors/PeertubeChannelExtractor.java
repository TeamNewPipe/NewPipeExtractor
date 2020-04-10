package org.schabi.newpipe.extractor.services.peertube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

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
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;

import static org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper.COUNT_KEY;
import static org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper.ITEMS_PER_PAGE;
import static org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper.START_KEY;

public class PeertubeChannelExtractor extends ChannelExtractor {
    private JsonObject json;
    private final String baseUrl;

    public PeertubeChannelExtractor(final StreamingService service, final ListLinkHandler linkHandler) throws ParsingException {
        super(service, linkHandler);
        this.baseUrl = getBaseUrl();
    }

    @Override
    public String getAvatarUrl() {
        String value;
        try {
            value = JsonUtils.getString(json, "avatar.path");
        } catch (Exception e) {
            value = "/client/assets/images/default-avatar.png";
        }
        return baseUrl + value;
    }

    @Override
    public String getBannerUrl() {
        return null;
    }

    @Override
    public String getFeedUrl() throws ParsingException {
        return getBaseUrl() + "/feeds/videos.xml?videoChannelId=" + json.get("id");
    }

    @Override
    public long getSubscriberCount() throws ParsingException {
        final Number number = JsonUtils.getNumber(json, "followersCount");
        return number.longValue();
    }

    @Override
    public String getDescription() {
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
    public String getParentChannelAvatarUrl() {
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
        final String pageUrl = getUrl() + "/videos?" + START_KEY + "=0&" + COUNT_KEY + "=" + ITEMS_PER_PAGE;
        return getPage(pageUrl);
    }

    private void collectStreamsFrom(final StreamInfoItemsCollector collector, final JsonObject json) throws ParsingException {
        final JsonArray contents;
        try {
            contents = (JsonArray) JsonUtils.getValue(json, "data");
        } catch (Exception e) {
            throw new ParsingException("unable to extract channel streams", e);
        }

        for (final Object c : contents) {
            if (c instanceof JsonObject) {
                final JsonObject item = (JsonObject) c;
                final PeertubeStreamInfoItemExtractor extractor = new PeertubeStreamInfoItemExtractor(item, baseUrl);
                collector.commit(extractor);
            }
        }
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(final String pageUrl) throws IOException, ExtractionException {
        final Response response = getDownloader().get(pageUrl);
        JsonObject json = null;
        if (response != null && !Utils.isBlank(response.responseBody())) {
            try {
                json = JsonParser.object().from(response.responseBody());
            } catch (Exception e) {
                throw new ParsingException("Could not parse json data for kiosk info", e);
            }
        }

        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        final long total;
        if (json != null) {
            PeertubeParsingHelper.validate(json);
            total = JsonUtils.getNumber(json, "total").longValue();
            collectStreamsFrom(collector, json);
        } else {
            throw new ExtractionException("Unable to get PeerTube kiosk info");
        }
        return new InfoItemsPage<>(collector, PeertubeParsingHelper.getNextPageUrl(pageUrl, total));
    }

    @Override
    public void onFetchPage(final Downloader downloader) throws IOException, ExtractionException {
        final Response response = downloader.get(getUrl());
        if (response != null && response.responseBody() != null) {
            setInitialData(response.responseBody());
        } else {
            throw new ExtractionException("Unable to extract PeerTube channel data");
        }
    }

    private void setInitialData(final String responseBody) throws ExtractionException {
        try {
            json = JsonParser.object().from(responseBody);
        } catch (JsonParserException e) {
            throw new ExtractionException("Unable to extract PeerTube channel data", e);
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
