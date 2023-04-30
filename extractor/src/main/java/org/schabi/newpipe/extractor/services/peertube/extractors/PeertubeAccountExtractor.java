package org.schabi.newpipe.extractor.services.peertube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper;
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeChannelLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

import static org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper.COUNT_KEY;
import static org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper.ITEMS_PER_PAGE;
import static org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper.START_KEY;
import static org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper.collectStreamsFrom;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public class PeertubeAccountExtractor extends ChannelExtractor {
    private JsonObject json;
    private final String baseUrl;
    private static final String ACCOUNTS = "accounts/";

    public PeertubeAccountExtractor(final StreamingService service,
                                    final ListLinkHandler linkHandler) throws ParsingException {
        super(service, linkHandler);
        this.baseUrl = getBaseUrl();
    }

    @Override
    public String getAvatarUrl() {
        String value;
        try {
            value = JsonUtils.getString(json, "avatar.path");
        } catch (final Exception e) {
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
        return getBaseUrl() + "/feeds/videos.xml?accountId=" + json.get("id");
    }

    @Override
    public long getSubscriberCount() throws ParsingException {
        // The subscriber count cannot be retrieved directly. It needs to be calculated.
        // An accounts subscriber count is the number of the channel owner's subscriptions
        // plus the sum of all sub channels subscriptions.
        long subscribersCount = json.getLong("followersCount");
        String accountVideoChannelUrl = baseUrl + PeertubeChannelLinkHandlerFactory.API_ENDPOINT;
        if (getId().contains(ACCOUNTS)) {
            accountVideoChannelUrl += getId();
        } else {
            accountVideoChannelUrl += ACCOUNTS + getId();
        }
        accountVideoChannelUrl += "/video-channels";

        try {
            final String responseBody = getDownloader().get(accountVideoChannelUrl).responseBody();
            final JsonObject jsonResponse = JsonParser.object().from(responseBody);
            final JsonArray videoChannels = jsonResponse.getArray("data");
            for (final Object videoChannel : videoChannels) {
                final JsonObject videoChannelJsonObject = (JsonObject) videoChannel;
                subscribersCount += videoChannelJsonObject.getInt("followersCount");
            }
        } catch (final IOException | JsonParserException | ReCaptchaException ignored) {
            // something went wrong during video channels extraction,
            // only return subscribers of ownerAccount
        }
        return subscribersCount;
    }

    @Nullable
    @Override
    public String getDescription() {
        return json.getString("description");
    }

    @Override
    public String getParentChannelName() {
        return "";
    }

    @Override
    public String getParentChannelUrl() {
        return "";
    }

    @Override
    public String getParentChannelAvatarUrl() {
        return "";
    }

    @Override
    public boolean isVerified() throws ParsingException {
        return false;
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws IOException, ExtractionException {
        return getPage(new Page(baseUrl + "/api/v1/" + getId() + "/videos?" + START_KEY + "=0&"
                + COUNT_KEY + "=" + ITEMS_PER_PAGE));
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(final Page page)
            throws IOException, ExtractionException {
        if (page == null || isNullOrEmpty(page.getUrl())) {
            throw new IllegalArgumentException("Page doesn't contain an URL");
        }

        final Response response = getDownloader().get(page.getUrl());

        JsonObject pageJson = null;
        if (response != null && !Utils.isBlank(response.responseBody())) {
            try {
                pageJson = JsonParser.object().from(response.responseBody());
            } catch (final Exception e) {
                throw new ParsingException("Could not parse json data for account info", e);
            }
        }

        if (pageJson != null) {
            PeertubeParsingHelper.validate(pageJson);
            final long total = pageJson.getLong("total");

            final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
            collectStreamsFrom(collector, pageJson, getBaseUrl());

            return new InfoItemsPage<>(collector,
                    PeertubeParsingHelper.getNextPage(page.getUrl(), total));
        } else {
            throw new ExtractionException("Unable to get PeerTube account info");
        }
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        String accountUrl = baseUrl + PeertubeChannelLinkHandlerFactory.API_ENDPOINT;
        if (getId().contains(ACCOUNTS)) {
            accountUrl += getId();
        } else {
            accountUrl += ACCOUNTS + getId();
        }

        final Response response = downloader.get(accountUrl);
        if (response != null) {
            setInitialData(response.responseBody());
        } else {
            throw new ExtractionException("Unable to extract PeerTube account data");
        }
    }

    private void setInitialData(final String responseBody) throws ExtractionException {
        try {
            json = JsonParser.object().from(responseBody);
        } catch (final JsonParserException e) {
            throw new ExtractionException("Unable to extract PeerTube account data", e);
        }
        if (json == null) {
            throw new ExtractionException("Unable to extract PeerTube account data");
        }
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return JsonUtils.getString(json, "displayName");
    }
}
