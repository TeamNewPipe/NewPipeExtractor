package org.schabi.newpipe.extractor.services.youtube.invidious.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeMixPlaylistExtractor;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousParsingHelper;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import javax.annotation.Nonnull;
import java.io.IOException;

public class InvidiousMixPlaylistExtractor extends PlaylistExtractor {

    private JsonObject json;
    private final String baseUrl;

    public InvidiousMixPlaylistExtractor(StreamingService service, ListLinkHandler linkHandler) {
        super(service, linkHandler);
        this.baseUrl = service.getInstance().getUrl();
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        final String apiUrl = baseUrl + "/api/v1/mixes/" + getId();
        final Response response = downloader.get(apiUrl);

        json = InvidiousParsingHelper.getValidJsonObjectFromResponse(response, apiUrl);
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return json.getString("title");
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws IOException, ExtractionException {
        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        final JsonArray videos = json.getArray("videos");
        for (Object o : videos) {
            final JsonObject video = (JsonObject) o;
            collector.commit(new InvidiousStreamInfoItemExtractor(video, baseUrl));
        }
        return new InfoItemsPage<>(collector, null);
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(Page page) throws IOException, ExtractionException {
        return InfoItemsPage.emptyPage();
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return YoutubeMixPlaylistExtractor.getThumbnailUrlFromPlaylistId(json.getString("mixId"));
    }

    @Override
    public String getBannerUrl() throws ParsingException {
        return "";
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        return "";
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return "YouTube";
    }

    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        return "";
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return false;
    }

    @Override
    public long getStreamCount() throws ParsingException {
        return ListExtractor.ITEM_COUNT_INFINITE;
    }

    @Nonnull
    @Override
    public String getSubChannelName() throws ParsingException {
        return "";
    }

    @Nonnull
    @Override
    public String getSubChannelUrl() throws ParsingException {
        return "";
    }

    @Nonnull
    @Override
    public String getSubChannelAvatarUrl() throws ParsingException {
        return "";
    }
}
