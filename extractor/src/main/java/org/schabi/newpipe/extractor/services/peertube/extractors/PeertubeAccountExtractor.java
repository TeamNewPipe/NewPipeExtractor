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
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.linkhandler.ChannelTabs;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeChannelLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeChannelTabLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.JsonUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

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

    @Override
    public String getDescription() {
        try {
            return JsonUtils.getString(json, "description");
        } catch (final ParsingException e) {
            return "No description";
        }
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
    public List<ListLinkHandler> getTabs() throws ParsingException {
        return Arrays.asList(
                PeertubeChannelTabLinkHandlerFactory.getInstance().fromQuery(getId(),
                        Collections.singletonList(ChannelTabs.VIDEOS), "", getBaseUrl()),
                PeertubeChannelTabLinkHandlerFactory.getInstance().fromQuery(getId(),
                        Collections.singletonList(ChannelTabs.CHANNELS), "", getBaseUrl())
        );
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        final Response response = downloader.get(baseUrl
                + PeertubeChannelLinkHandlerFactory.API_ENDPOINT + getId());
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
