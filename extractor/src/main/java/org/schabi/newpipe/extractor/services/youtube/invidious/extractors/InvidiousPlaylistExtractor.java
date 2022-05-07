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
        return InvidiousParsingHelper.getThumbnailUrl(json.getArray("authorThumbnails"));
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
        return json.getLong("videoCount", -1);
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws IOException, ExtractionException {
        return getPage(getPageByIndex(0));
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(
            final Page page
    ) throws IOException, ExtractionException {
        if (page == null) {
            return InfoItemsPage.emptyPage();
        }
        // Initial fetched page (onFetchPage) already contains these info so don't fetch again
        if (Integer.parseInt(page.getId()) != 0) {
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

        final int lastIndex = videos.isEmpty()
                ? -1
                : videos.getObject(videos.size() - 1).getInt("index", -1);

        final Page nextPage = lastIndex == -1 || lastIndex >= getStreamCount() - 1
                ? null
                : getPageByIndex(lastIndex);

        return new InfoItemsPage<>(collector, nextPage);
    }

    /*
     * Note: Querying is done by index and not pagination, because it's a lot easier
     * // CHECKSTYLE:OFF - url has to be there in one piece
     * https://github.com/iv-org/invidious/blob/4900ce24fac163d801a56af1fcf0f4c207448adf/src/invidious/routes/api/v1/misc.cr#L20-L22
     * // CHECKSTYLE:ON
     *
     * e.g. Paging contains multiple duplicate items:
     * Playlist-Size=505
     * Page StartIndex EndIndex Video-Count
     * 1 0   199 200
     * 2 50  249 200
     * 3 150 349 200
     * 4 250 449 200
     * 5 350 504 154
     * 6 450 504 54
     * 7 -
     *
     * Also note that the index is also used as offset
     */
    public Page getPageByIndex(final int index) throws ParsingException {
        return new Page(
                baseUrl + "/api/v1/playlists/" + getId() + "?index=" + index,
                String.valueOf(index));
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
