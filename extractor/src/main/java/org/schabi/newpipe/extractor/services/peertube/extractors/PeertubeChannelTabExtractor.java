package org.schabi.newpipe.extractor.services.peertube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.MultiInfoItemsCollector;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelTabExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ChannelTabHandler;
import org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper;
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeChannelLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

import static org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper.*;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public class PeertubeChannelTabExtractor extends ChannelTabExtractor {
    private JsonObject initialJson;
    private final String baseUrl;
    private String initialUrl;

    public PeertubeChannelTabExtractor(final StreamingService service,
                                       final ChannelTabHandler linkHandler) throws ParsingException {
        super(service, linkHandler);
        baseUrl = getBaseUrl();
        initialUrl = this.baseUrl + PeertubeChannelLinkHandlerFactory.API_ENDPOINT + getId() + "/video-playlists?"
                + START_KEY + "=0&" + COUNT_KEY + "=" + ITEMS_PER_PAGE;
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        if (getLinkHandler().getTab() != ChannelTabHandler.Tab.Playlists) {
            throw new ExtractionException("tab " + getLinkHandler().getTab().name() + " not supported");
        }

        final Response response = downloader.get(initialUrl);
        if (response != null) {
            setInitialData(response.responseBody());
        } else {
            throw new ExtractionException("Unable to extract PeerTube channel playlist data");
        }
    }

    private void setInitialData(final String responseBody) throws ExtractionException {
        try {
            initialJson = JsonParser.object().from(responseBody);
        } catch (final JsonParserException e) {
            throw new ExtractionException("Unable to extract PeerTube channel playlist data", e);
        }
        if (initialJson == null) {
            throw new ExtractionException("Unable to extract PeerTube channel playlist data");
        }
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return initialJson.getArray("data").getObject(0)
                .getObject("ownerAccount").getString("displayName", "");
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws IOException, ExtractionException {
        return parsePage(initialJson, initialUrl);
    }

    @Override
    public InfoItemsPage<InfoItem> getPage(Page page) throws IOException, ExtractionException {
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

        return parsePage(pageJson, page.getUrl());
    }

    private InfoItemsPage<InfoItem> parsePage(@Nullable JsonObject json, String pageUrl)
            throws ExtractionException {
        if (json == null) {
            throw new ExtractionException("Unable to get channel playlist list");
        }

        PeertubeParsingHelper.validate(json);

        final MultiInfoItemsCollector collector = new MultiInfoItemsCollector(getServiceId());
        JsonArray contents = json.getArray("data");
        if (contents == null) {
            throw new ParsingException("Unable to extract channel playlist list");
        }

        for (final Object c : contents) {
            if (c instanceof JsonObject) {
                collector.commit(new PeertubePlaylistInfoItemExtractor((JsonObject) c, baseUrl));
            }
        }

        return new InfoItemsPage<>(
                collector, PeertubeParsingHelper.getNextPage(pageUrl, json.getLong("total")));
    }
}
