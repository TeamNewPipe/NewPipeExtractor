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

import javax.annotation.Nonnull;
import java.io.IOException;

import static org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper.*;

public class PeertubePlaylistExtractor extends PlaylistExtractor {

    private JsonObject playlistInfo;
    private JsonObject playlistVideos;
    private String initialPageUrl;

    private long total;

    public PeertubePlaylistExtractor(StreamingService service, ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return getBaseUrl() + playlistInfo.getString("thumbnailPath");
    }

    @Override
    public String getBannerUrl() throws ParsingException {
        return null;
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        return playlistInfo.getObject("ownerAccount").getString("url");
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return playlistInfo.getObject("ownerAccount").getString("displayName");
    }

    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        return getBaseUrl() + playlistInfo.getObject("ownerAccount").getObject("avatar").getString("path");
    }

    @Override
    public long getStreamCount() throws ParsingException {
        return playlistInfo.getNumber("videosLength").longValue();
    }

    @Nonnull
    @Override
    public String getSubChannelName() throws ParsingException {
        return playlistInfo.getObject("videoChannel").getString("displayName");
    }

    @Nonnull
    @Override
    public String getSubChannelUrl() throws ParsingException {
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
    public String getNextPageUrl() throws IOException, ExtractionException {
        return PeertubeParsingHelper.getNextPageUrl(initialPageUrl, total);
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(String pageUrl) throws IOException, ExtractionException {
        Response response = getDownloader().get(pageUrl);
        try {
            playlistVideos = JsonParser.object().from(response.responseBody());
        } catch (JsonParserException jpe) {
            throw new ExtractionException("Could not parse json", jpe);
        }
        PeertubeParsingHelper.validate(playlistVideos);

        this.total = JsonUtils.getNumber(playlistVideos, "total").longValue();

        StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

        JsonArray videos = playlistVideos.getArray("data");
        for (Object o : videos) {
            JsonObject video = ((JsonObject) o).getObject("video");
            collector.commit(new PeertubeStreamInfoItemExtractor(video, getBaseUrl()));
        }

        return new InfoItemsPage<>(collector, PeertubeParsingHelper.getNextPageUrl(pageUrl, total));
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        Response response = downloader.get(getUrl());
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
