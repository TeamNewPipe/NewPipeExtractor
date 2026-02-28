package org.schabi.newpipe.extractor.services.youtube.extractors;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getUrlFromObject;

import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs;
import org.schabi.newpipe.extractor.channel.tabs.rendererlist.RendererListInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelTabLinkHandlerFactory;

import java.util.List;

import javax.annotation.Nonnull;

public class YoutubeShelfRendererListInfoItemExtractor implements RendererListInfoItemExtractor {

    public static final String FEATURED_CHANNEL_LIST = "FEATURED_CHANNELS_LIST";
    private final JsonObject rendererListInfoItem;
    private final int parentListIndex;

    private final String rendererListItemType;

    public YoutubeShelfRendererListInfoItemExtractor(final JsonObject rendererListInfoItem,
            final int parentListIndex, final String rendererListItemType) {
        this.rendererListInfoItem = rendererListInfoItem;
        this.parentListIndex = parentListIndex;
        this.rendererListItemType = rendererListItemType;
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

                if (uploaderTabURL != null) {
                    return uploaderTabURL + "/rendererlist/" + parentListIndex; // virtual url
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
    public int getParentListIndex() throws ParsingException {
        return parentListIndex;
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
