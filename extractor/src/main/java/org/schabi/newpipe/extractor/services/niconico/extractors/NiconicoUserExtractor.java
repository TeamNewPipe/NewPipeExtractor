package org.schabi.newpipe.extractor.services.niconico.extractors;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import java.io.IOException;

import javax.annotation.Nonnull;

public class NiconicoUserExtractor extends ChannelExtractor {
    private Document rss;
    private Document user;
    private JsonObject info;

    public NiconicoUserExtractor(StreamingService service, ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        final String url = getLinkHandler().getUrl() + "?rss=2.0";
        rss = Jsoup.parse(getDownloader().get(url).responseBody());

        user = Jsoup.parse(getDownloader().get(getLinkHandler().getUrl()).responseBody());
        try {
            info = JsonParser.object()
                    .from(user.getElementById("js-initial-userpage-data")
                            .attr("data-initial-data"));
        } catch (JsonParserException e) {
            throw new ExtractionException("could not parse user information.");
        }
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return info.getObject("userDetails").getObject("userDetails")
                .getObject("user").getString("nickname");
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws IOException, ExtractionException {
        return null;
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(Page page) throws IOException, ExtractionException {
        return null;
    }

    @Override
    public String getAvatarUrl() throws ParsingException {
        return info.getObject("userDetails").getObject("userDetails")
                .getObject("user").getObject("icons").getString("large");
    }

    @Override
    public String getBannerUrl() throws ParsingException {
        // Niconico does not have user banner.
        return null;
    }

    @Override
    public String getFeedUrl() throws ParsingException {
        return getLinkHandler().getUrl() + "?rss=2.0";
    }

    @Override
    public long getSubscriberCount() throws ParsingException {
        return info.getObject("userDetails").getObject("userDetails")
                .getObject("user").getLong("followerCount");
    }

    @Override
    public String getDescription() throws ParsingException {
        return info.getObject("userDetails").getObject("userDetails")
                .getObject("user").getString("description");
    }

    @Override
    public String getParentChannelName() throws ParsingException {
        return null;
    }

    @Override
    public String getParentChannelUrl() throws ParsingException {
        return null;
    }

    @Override
    public String getParentChannelAvatarUrl() throws ParsingException {
        return null;
    }

    @Override
    public boolean isVerified() throws ParsingException {
        return false;
    }
}
