package org.schabi.newpipe.extractor.services.media_ccc.extractors;

import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.ServiceList;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCConferenceLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamInfoItemExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import javax.annotation.Nullable;

import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

public class MediaCCCRecentKioskExtractor implements StreamInfoItemExtractor {

    private final JsonObject event;
    private final Map<String, String> conferenceNames;

    public MediaCCCRecentKioskExtractor(final JsonObject event,
                                        final Map<String, String> conferenceNames) {
        this.event = event;
        this.conferenceNames = conferenceNames;
    }

    @Override
    public String getName() throws ParsingException {
        return event.getString("title");
    }

    @Override
    public String getUrl() throws ParsingException {
        return event.getString("frontend_link");
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return event.getString("thumb_url");
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
    public long getDuration() throws ParsingException {
        return event.getInt("duration");
    }

    @Override
    public long getViewCount() throws ParsingException {
        return event.getInt("view_count");
    }

    @Override
    public String getUploaderName() throws ParsingException {
        final String conferenceApiUrl = event.getString("conference_url");
        if (isNullOrEmpty(conferenceApiUrl)) {
            throw new ParsingException("conference url is empty");
        }

        if (conferenceNames.containsKey(conferenceApiUrl)) {
            return conferenceNames.get(conferenceApiUrl);
        }

        // get conference name from API.
        try {
            ChannelExtractor extractor = ServiceList.MediaCCC.getChannelExtractor(
                    new MediaCCCConferenceLinkHandlerFactory().fromUrl(conferenceApiUrl));
            extractor.fetchPage();
            conferenceNames.put(conferenceApiUrl, extractor.getName());
            return extractor.getName();
        } catch (IOException | ExtractionException e) {
            throw new ParsingException("Could not get conference name from conference API URL", e);
        }
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        return new MediaCCCConferenceLinkHandlerFactory()
                .fromUrl(event.getString("conference_url")) // API URL
                .getUrl(); // web URL
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
