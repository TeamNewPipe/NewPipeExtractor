package org.schabi.newpipe.extractor.services.youtube.invidious.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousService;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import java.io.IOException;

import javax.annotation.Nonnull;

public class InvidiousPlaylistExtractor extends PlaylistExtractor {

    private JsonObject json;
    private final String baseUrl;

    public InvidiousPlaylistExtractor(
            final InvidiousService service,
            final ListLinkHandler linkHandler) {
        super(service, linkHandler);
        this.baseUrl = service.getInstance().getUrl();
    }

    @Nonnull
    @Override
    public String getThumbnailUrl() {
        final JsonArray thumbnails = json.getArray("authorThumbnails");
        return InvidiousParsingHelper.getThumbnailUrl(thumbnails);
    }

    @Override
    public String getUploaderUrl() {
        return baseUrl + "/channel/" + json.getString("authorId");
    }

    @Override
    public String getUploaderName() {
        return json.getString("author");
    }

    @Override
    public String getUploaderAvatarUrl() {
        return InvidiousParsingHelper.getThumbnailUrl(json.getArray("authorThumbnails"));
    }

    @Override
    public long getStreamCount() {
        final Number number = json.getNumber("videoCount");
        return number == null ? -1 : number.longValue();
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws IOException, ExtractionException {
        return getPage(getPage(1));
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(
            final Page page
    ) throws IOException, ExtractionException {
        if (Integer.parseInt(page.getId()) != 1) {
            final Response rp = NewPipe.getDownloader().get(page.getUrl());
            json = InvidiousParsingHelper.getValidJsonObjectFromResponse(rp, page.getUrl());
        }

        final JsonArray videos = json.getArray("videos");

        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        videos.stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .map(commentObj -> new InvidiousStreamInfoItemExtractor(commentObj, baseUrl))
                .forEach(collector::commit);

        final Page nextPage = videos.size() < 99
                // max number of items per page
                ? null
                : getPage(Integer.parseInt(page.getId()) + 1);


        return new InfoItemsPage<>(collector, nextPage);
    }

    public Page getPage(final int page) throws ParsingException {
        return InvidiousParsingHelper.getPage(baseUrl + "/api/v1/playlists/" + getId(), page);
    }

    @Override
    public void onFetchPage(
            @Nonnull final Downloader downloader
    ) throws IOException, ExtractionException {
        final String apiUrl = baseUrl + "/api/v1/playlists/" + getId();

        final Response response = downloader.get(apiUrl);

        json = InvidiousParsingHelper.getValidJsonObjectFromResponse(response, apiUrl);
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return json.getString("title");
    }
}
