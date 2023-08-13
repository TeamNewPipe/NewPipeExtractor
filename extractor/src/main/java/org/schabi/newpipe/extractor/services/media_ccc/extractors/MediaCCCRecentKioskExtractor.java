package org.schabi.newpipe.extractor.services.media_ccc.extractors;

import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCConferenceLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCParsingHelper.getImageListFromLogoImageUrl;

public class MediaCCCRecentKioskExtractor implements StreamInfoItemExtractor {

    private final JsonObject event;

    public MediaCCCRecentKioskExtractor(final JsonObject event) {
        this.event = event;
    }

    @Override
    public String getName() throws ParsingException {
        return event.getString("title");
    }

    @Override
    public String getUrl() throws ParsingException {
        return event.getString("frontend_link");
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() throws ParsingException {
        return getImageListFromLogoImageUrl(event.getString("poster_url"));
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public boolean isAd() {
        return false;
    }

    @Override
    public long getDuration() {
        // duration and length have the same value, see
        // https://github.com/voc/voctoweb/blob/master/app/views/public/shared/_event.json.jbuilder
        return event.getInt("duration");
    }

    @Override
    public long getViewCount() throws ParsingException {
        return event.getInt("view_count");
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return event.getString("conference_title");
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        return MediaCCCConferenceLinkHandlerFactory.getInstance()
                .fromUrl(event.getString("conference_url")) // API URL
                .getUrl(); // web URL
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return false;
    }

    @Nullable
    @Override
    public String getTextualUploadDate() throws ParsingException {
        return event.getString("date");
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        final ZonedDateTime zonedDateTime = ZonedDateTime.parse(event.getString("date"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSzzzz"));
        return new DateWrapper(zonedDateTime.toOffsetDateTime(), false);
    }
}
