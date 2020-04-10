package org.schabi.newpipe.extractor.services.peertube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.utils.JsonUtils;

import java.io.IOException;

import javax.annotation.Nonnull;

import static org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper.COUNT_KEY;
import static org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper.ITEMS_PER_PAGE;
import static org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper.START_KEY;

public class PeertubePlaylistExtractor extends PlaylistExtractor {
    private JsonObject playlistInfo;
    private String initialPageUrl;

    public PeertubePlaylistExtractor(final StreamingService service, final ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return getBaseUrl() + playlistInfo.getString("thumbnailPath");
    }

    @Override
    public String getBannerUrl() {
        return null;
    }

    @Override
    public String getUploaderUrl() {
        return playlistInfo.getObject("ownerAccount").getString("url");
    }

    @Override
    public String getUploaderName() {
        return playlistInfo.getObject("ownerAccount").getString("displayName");
    }

    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        return getBaseUrl() + playlistInfo.getObject("ownerAccount").getObject("avatar").getString("path");
    }

    @Override
    public long getStreamCount() {
        return playlistInfo.getNumber("videosLength").longValue();
    }

    @Nonnull
    @Override
    public String getSubChannelName() {
        return playlistInfo.getObject("videoChannel").getString("displayName");
    }

    @Nonnull
    @Override
    public String getSubChannelUrl() {
        return playlistInfo.getObject("videoChannel").getString("url");
    }

    @Nonnull
    @Override
    public String getSubChannelAvatarUrl() throws ParsingException {
        return getBaseUrl() + playlistInfo.getObject("videoChannel").getObject("avatar").getString("path");
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws IOException, ExtractionException {
        return getPage(initialPageUrl);
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(final String pageUrl) throws IOException, ExtractionException {
        final Response response = getDownloader().get(pageUrl);
        final JsonObject playlistVideos;
        try {
            playlistVideos = JsonParser.object().from(response.responseBody());
        } catch (JsonParserException jpe) {
            throw new ExtractionException("Could not parse json", jpe);
        }
        PeertubeParsingHelper.validate(playlistVideos);

        final long total = JsonUtils.getNumber(playlistVideos, "total").longValue();

        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

        final JsonArray videos = playlistVideos.getArray("data");
        for (final Object o : videos) {
            final JsonObject video = ((JsonObject) o).getObject("video");
            collector.commit(new PeertubeStreamInfoItemExtractor(video, getBaseUrl()));
        }

        return new InfoItemsPage<>(collector, PeertubeParsingHelper.getNextPageUrl(pageUrl, total));
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) throws IOException, ExtractionException {
        final Response response = downloader.get(getUrl());
        try {
            playlistInfo = JsonParser.object().from(response.responseBody());
        } catch (JsonParserException jpe) {
            throw new ExtractionException("Could not parse json", jpe);
        }
        PeertubeParsingHelper.validate(playlistInfo);
        initialPageUrl = getUrl() + "/videos?" + START_KEY + "=0&" + COUNT_KEY + "=" + ITEMS_PER_PAGE;
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return playlistInfo.getString("displayName");
    }
}
