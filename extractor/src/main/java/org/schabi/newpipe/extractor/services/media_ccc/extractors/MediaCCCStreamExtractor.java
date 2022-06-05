package org.schabi.newpipe.extractor.services.media_ccc.extractors;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCConferenceLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.streamdata.delivery.simpleimpl.SimpleProgressiveHTTPDeliveryDataImpl;
import org.schabi.newpipe.extractor.streamdata.format.registry.AudioFormatRegistry;
import org.schabi.newpipe.extractor.streamdata.format.registry.VideoAudioFormatRegistry;
import org.schabi.newpipe.extractor.streamdata.stream.AudioStream;
import org.schabi.newpipe.extractor.streamdata.stream.VideoAudioStream;
import org.schabi.newpipe.extractor.streamdata.stream.quality.VideoQualityData;
import org.schabi.newpipe.extractor.streamdata.stream.simpleimpl.SimpleAudioStreamImpl;
import org.schabi.newpipe.extractor.streamdata.stream.simpleimpl.SimpleVideoAudioStreamImpl;
import org.schabi.newpipe.extractor.utils.JsonUtils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

public class MediaCCCStreamExtractor extends StreamExtractor {
    private JsonObject data;
    private JsonObject conferenceData;

    public MediaCCCStreamExtractor(final StreamingService service, final LinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Nonnull
    @Override
    public String getTextualUploadDate() {
        return data.getString("release_date");
    }

    @Nonnull
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return new DateWrapper(MediaCCCParsingHelper.parseDateFrom(getTextualUploadDate()));
    }

    @Nonnull
    @Override
    public String getThumbnailUrl() {
        return data.getString("thumb_url");
    }

    @Nonnull
    @Override
    public Description getDescription() {
        return new Description(data.getString("description"), Description.PLAIN_TEXT);
    }

    @Override
    public long getLength() {
        return data.getInt("length");
    }

    @Override
    public long getViewCount() {
        return data.getInt("view_count");
    }

    @Nonnull
    @Override
    public String getUploaderUrl() {
        return MediaCCCConferenceLinkHandlerFactory.CONFERENCE_PATH + getUploaderName();
    }

    @Nonnull
    @Override
    public String getUploaderName() {
        return data.getString("conference_url")
                .replaceFirst("https://(api\\.)?media\\.ccc\\.de/public/conferences/", "");
    }

    @Nonnull
    @Override
    public String getUploaderAvatarUrl() {
        return conferenceData.getString("logo_url");
    }

    @Override
    public List<AudioStream> getAudioStreams() throws ExtractionException {
        return getRecordingsByMimeType("audio")
                .map(o -> new SimpleAudioStreamImpl(
                        new SimpleProgressiveHTTPDeliveryDataImpl(o.getString("recording_url")),
                        new AudioFormatRegistry().getFromMimeType(o.getString("mime_type"))
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<VideoAudioStream> getVideoStreams() throws ExtractionException {
        return getRecordingsByMimeType("video")
                .map(o -> new SimpleVideoAudioStreamImpl(
                        new SimpleProgressiveHTTPDeliveryDataImpl(o.getString("recording_url")),
                        null,
                        AudioStream.UNKNOWN_BITRATE,
                        new VideoAudioFormatRegistry().getFromMimeType(o.getString("mime_type")),
                        VideoQualityData.fromHeightWidth(
                                o.getInt("height", VideoQualityData.UNKNOWN),
                                o.getInt("width", VideoQualityData.UNKNOWN))
                ))
                .collect(Collectors.toList());
    }

    private Stream<JsonObject> getRecordingsByMimeType(final String startsWithMimeType) {
        return data.getArray("recordings").stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .filter(rec -> rec.getString("mime_type", "")
                        .startsWith(startsWithMimeType));
    }

    @Override
    public StreamType getStreamType() {
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        final String videoUrl = MediaCCCStreamLinkHandlerFactory.VIDEO_API_ENDPOINT + getId();
        try {
            data = JsonParser.object().from(downloader.get(videoUrl).responseBody());
            conferenceData = JsonParser.object()
                    .from(downloader.get(data.getString("conference_url")).responseBody());
        } catch (final JsonParserException jpe) {
            throw new ExtractionException("Could not parse json returned by URL: " + videoUrl, jpe);
        }
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return data.getString("title");
    }

    @Nonnull
    @Override
    public String getOriginalUrl() {
        return data.getString("frontend_link");
    }

    @Override
    public Locale getLanguageInfo() throws ParsingException {
        return Localization.getLocaleFromThreeLetterCode(data.getString("original_language"));
    }

    @Nonnull
    @Override
    public List<String> getTags() {
        return JsonUtils.getStringListFromJsonArray(data.getArray("tags"));
    }
}
