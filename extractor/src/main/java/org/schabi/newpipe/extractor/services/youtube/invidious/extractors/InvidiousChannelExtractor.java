package org.schabi.newpipe.extractor.services.youtube.invidious.extractors;

import static org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousParsingHelper.getUid;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousService;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;

import java.io.IOException;

import javax.annotation.Nonnull;


public class InvidiousChannelExtractor extends ChannelExtractor {

    private final String baseUrl;
    private JsonObject json;

    public InvidiousChannelExtractor(
            final InvidiousService service,
            final ListLinkHandler linkHandler
    ) {
        super(service, linkHandler);
        this.baseUrl = service.getInstance().getUrl();
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws IOException, ExtractionException {
        return getPage(getPage(1));
    }

    protected Page getPage(final int page) {
        return InvidiousParsingHelper.getPage(
                baseUrl + "/api/v1/channels/videos/" + json.getString("authorId"),
                page
        );
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(final Page page) throws IOException, ExtractionException {
        final Response rp = NewPipe.getDownloader().get(page.getUrl());
        final JsonArray array =
                InvidiousParsingHelper.getValidJsonArrayFromResponse(rp, page.getUrl());

        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        array.stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .map(o -> new InvidiousStreamInfoItemExtractor(o, baseUrl))
                .forEach(collector::commit);

        final Page nextPage = array.size() < 59
                // max number of items per page
                // with Second it is 29 but next Page logic is not implemented
                ? null
                : getPage(Integer.parseInt(page.getId()) + 1);

        return new InfoItemsPage<>(collector, nextPage);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) throws IOException, ExtractionException {
        final String apiUrl = baseUrl + "/api/v1/channels/" + getUid(getId())
                + "?fields=author,description,subCount,authorThumbnails,authorBanners,authorId"
                + "&region=" + getExtractorContentCountry().getCountryCode();

        final Response response = downloader.get(apiUrl);

        json = InvidiousParsingHelper.getValidJsonObjectFromResponse(response, apiUrl);
    }

    @Override
    public String getAvatarUrl() {
        return InvidiousParsingHelper.getThumbnailUrl(json.getArray("authorThumbnails"));
    }

    @Override
    public String getBannerUrl() {
        return InvidiousParsingHelper.getThumbnailUrl(json.getArray("authorBanners"));
    }

    @Override
    public String getFeedUrl() {
        return baseUrl + "/feed/channel/" + json.getString("authorId");
    }

    @Override
    public long getSubscriberCount() {
        return json.getNumber("subCount").longValue();
    }

    @Override
    public String getDescription() {
        return json.getString("description");
    }

    @Nonnull
    @Override
    public String getName() {
        return json.getString("author");
    }
}
