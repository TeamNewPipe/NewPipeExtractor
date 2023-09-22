/*
 * Created by Christian Schabesberger on 12.02.17.
 *
 * Copyright (C) 2017 Christian Schabesberger <chris.schabesberger@mailbox.org>
 * YoutubeChannelInfoItemExtractor.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor. If not, see <https://www.gnu.org/licenses/>.
 */

package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.ListExtractor;
import org.schabi.newpipe.extractor.channel.ChannelInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nonnull;
import java.util.List;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getThumbnailsFromInfoItem;

public class YoutubeChannelInfoItemExtractor implements ChannelInfoItemExtractor {
    private final JsonObject channelInfoItem;
    /**
     * New layout:
     * "subscriberCountText": Channel handle
     * "videoCountText": Subscriber count
     */
    private final boolean withHandle;

    public YoutubeChannelInfoItemExtractor(final JsonObject channelInfoItem) {
        this.channelInfoItem = channelInfoItem;

        boolean wHandle = false;
        final String subscriberCountText = getTextFromObject(
                channelInfoItem.getObject("subscriberCountText"));
        if (subscriberCountText != null) {
            wHandle = subscriberCountText.startsWith("@");
        }
        this.withHandle = wHandle;
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() throws ParsingException {
        try {
            return getThumbnailsFromInfoItem(channelInfoItem);
        } catch (final Exception e) {
            throw new ParsingException("Could not get thumbnails", e);
        }
    }

    @Override
    public String getName() throws ParsingException {
        try {
            return getTextFromObject(channelInfoItem.getObject("title"));
        } catch (final Exception e) {
            throw new ParsingException("Could not get name", e);
        }
    }

    @Override
    public String getUrl() throws ParsingException {
        try {
            final String id = "channel/" + channelInfoItem.getString("channelId");
            return YoutubeChannelLinkHandlerFactory.getInstance().getUrl(id);
        } catch (final Exception e) {
            throw new ParsingException("Could not get url", e);
        }
    }

    @Override
    public long getSubscriberCount() throws ParsingException {
        try {
            if (!channelInfoItem.has("subscriberCountText")) {
                // Subscription count is not available for this channel item.
                return -1;
            }

            if (withHandle) {
                if (channelInfoItem.has("videoCountText")) {
                    return Utils.mixedNumberWordToLong(getTextFromObject(
                            channelInfoItem.getObject("videoCountText")));
                } else {
                    return -1;
                }
            }

            return Utils.mixedNumberWordToLong(getTextFromObject(
                    channelInfoItem.getObject("subscriberCountText")));
        } catch (final Exception e) {
            throw new ParsingException("Could not get subscriber count", e);
        }
    }

    @Override
    public long getStreamCount() throws ParsingException {
        try {
            if (withHandle || !channelInfoItem.has("videoCountText")) {
                // Video count is not available, either the channel has no public uploads
                // or YouTube displays the channel handle instead.
                return ListExtractor.ITEM_COUNT_UNKNOWN;
            }

            return Long.parseLong(Utils.removeNonDigitCharacters(getTextFromObject(
                    channelInfoItem.getObject("videoCountText"))));
        } catch (final Exception e) {
            throw new ParsingException("Could not get stream count", e);
        }
    }

    @Override
    public boolean isVerified() throws ParsingException {
        return YoutubeParsingHelper.isVerified(channelInfoItem.getArray("ownerBadges"));
    }

    @Override
    public String getDescription() throws ParsingException {
        try {
            if (!channelInfoItem.has("descriptionSnippet")) {
                // Channel have no description.
                return null;
            }

            return getTextFromObject(channelInfoItem.getObject("descriptionSnippet"));
        } catch (final Exception e) {
            throw new ParsingException("Could not get description", e);
        }
    }
}
