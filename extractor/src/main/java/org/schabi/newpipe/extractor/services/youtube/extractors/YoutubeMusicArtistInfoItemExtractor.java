package org.schabi.newpipe.extractor.services.youtube.extractors;

import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.channel.ChannelInfoItemExtractor;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getTextFromObject;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getImagesFromThumbnailsArray;
import static org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper.getUrlFromNavigationEndpoint;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public class YoutubeMusicArtistInfoItemExtractor implements ChannelInfoItemExtractor {
    private final JsonObject artistInfoItem;

    public YoutubeMusicArtistInfoItemExtractor(final JsonObject artistInfoItem) {
        this.artistInfoItem = artistInfoItem;
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() throws ParsingException {
        try {
            return getImagesFromThumbnailsArray(
                    artistInfoItem.getObject("thumbnail")
                            .getObject("musicThumbnailRenderer")
                            .getObject("thumbnail")
                            .getArray("thumbnails"));
        } catch (final Exception e) {
            throw new ParsingException("Could not get thumbnails", e);
        }
    }

    @Override
    public String getName() throws ParsingException {
        final String name = getTextFromObject(artistInfoItem.getArray("flexColumns")
                .getObject(0)
                .getObject("musicResponsiveListItemFlexColumnRenderer")
                .getObject("text"));
        if (!isNullOrEmpty(name)) {
            return name;
        }
        throw new ParsingException("Could not get name");
    }

    @Override
    public String getUrl() throws ParsingException {
        final String url = getUrlFromNavigationEndpoint(
                artistInfoItem.getObject("navigationEndpoint"));
        if (!isNullOrEmpty(url)) {
            return url;
        }
        throw new ParsingException("Could not get URL");
    }

    @Override
    public long getSubscriberCount() throws ParsingException {
        final String subscriberCount = getTextFromObject(artistInfoItem.getArray("flexColumns")
                .getObject(2)
                .getObject("musicResponsiveListItemFlexColumnRenderer")
                .getObject("text"));
        if (!isNullOrEmpty(subscriberCount)) {
            try {
                return Utils.mixedNumberWordToLong(subscriberCount);
            } catch (final Parser.RegexException ignored) {
                // probably subscriberCount == "No subscribers" or similar
                return 0;
            }
        }
        throw new ParsingException("Could not get subscriber count");
    }

    @Override
    public long getStreamCount() {
        return -1;
    }

    @Override
    public boolean isVerified() {
        // An artist on YouTube Music is always verified
        return true;
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }
}
