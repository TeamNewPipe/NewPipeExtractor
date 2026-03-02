package org.schabi.newpipe.extractor.services.youtube.extractors;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getUrlFromObject;

import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.channel.tabs.rendererlist.RendererListInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelTabLinkHandlerFactory;

import java.util.List;

import javax.annotation.Nonnull;

public class YoutubeShelfRendererListInfoItemExtractor implements RendererListInfoItemExtractor {

    public static final String FEATURED_CHANNEL_LIST = "FEATURED_CHANNELS_LIST";
    private final JsonObject rendererListInfoItem;
    private final String rendererListItemType;

    private final String id;
    private final List<String> contentFilter;

    public YoutubeShelfRendererListInfoItemExtractor(final JsonObject rendererListInfoItem,
                                                     final String rendererListItemType,
                                                     final String id,
                                                     final String tab,
                                                     final int index) {
        this.rendererListInfoItem = rendererListInfoItem;
        this.rendererListItemType = rendererListItemType;
        this.contentFilter = List.of(
                tab,
                RendererListInfoItemExtractor
                        .getRendererListIndexContentFilter(index));
        this.id = id;
    }


    @Override
    public String getName() throws ParsingException {
        try {
            final JsonObject title = this.rendererListInfoItem.getObject("title");
            String name = getTextFromObject(title);

            if (name == null && this.rendererListInfoItem.isString("title")) {
                name = this.rendererListInfoItem.getString("title");
            }

            return name;
        } catch (final Exception e) {
            throw new ParsingException("Could not get name", e);
        }
    }

    @Override
    public String getUrl() throws ParsingException {
        try {
            final String url = getUrlFromObject(rendererListInfoItem.getObject("title"));

            if (url == null) {
                final String uploaderTabURL = getUploaderTabUrl();

                if (uploaderTabURL != null && contentFilter.get(1) != null) {
                    final int index = YoutubeParsingHelper
                            .parseRendererListIndexParam(contentFilter.get(1));
                    // virtual url since they are a list
                    return uploaderTabURL + "/rendererlist/" + index;
                }
            }

            return url;
        } catch (final Exception e) {
            throw new ParsingException("Could not get uploader url", e);
        }
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() throws ParsingException {
        return List.of();
    }

    @Override
    public ListLinkHandler getListLinkHandler() throws ParsingException {
        final String tabUrl = getUploaderTabUrl();
        return new ListLinkHandler(tabUrl, tabUrl, this.id, this.contentFilter, "");
    }

    @Override
    public String getRendererListItemType() throws ParsingException {
        return this.rendererListItemType;
    }

    @Override
    public String getUploaderTabUrl() throws ParsingException {
        try {
            final String uploaderUrl = getUploaderUrl();
            if (uploaderUrl != null) {
                return uploaderUrl
                        + YoutubeChannelTabLinkHandlerFactory
                                .getUrlSuffix(ChannelTabs.FEATURED);
            }

            return null;
        } catch (final Exception e) {
            throw new ParsingException("Could not get uploader url", e);
        }
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return null; // will not exist in rendererListInfoItem
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        return null; // will not exist in rendererListInfoItem
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return false; // will not exist in rendererListInfoItem
    }
}
