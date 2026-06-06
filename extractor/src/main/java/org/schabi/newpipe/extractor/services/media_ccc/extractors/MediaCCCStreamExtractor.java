package org.schabi.newpipe.extractor.services.media_ccc.extractors;

import static org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCParsingHelper.getImageListFromLogoImageUrl;
import static org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCParsingHelper.getThumbnailsFromStreamItem;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCConferenceLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.StreamingProtocol;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.schabi.newpipe.extractor.stream.deliverysource.uri.HttpDeliverySource;
import org.schabi.newpipe.extractor.stream.deliverysource.uri.UriObject;
import org.schabi.newpipe.extractor.stream.impl.UriAudioStream;
import org.schabi.newpipe.extractor.stream.impl.UriMuxedStream;
import org.schabi.newpipe.extractor.stream.impl.UriSubtitlesStream;
import org.schabi.newpipe.extractor.stream.impl.base.BaseAudioStreamImpl;
import org.schabi.newpipe.extractor.stream.impl.base.BaseVideoStreamImpl;
import org.schabi.newpipe.extractor.stream.interfaces.Stream;
import org.schabi.newpipe.extractor.stream.interfaces.base.BaseAudioStream;
import org.schabi.newpipe.extractor.stream.interfaces.base.BaseVideoStream;
import org.schabi.newpipe.extractor.stream.mediaformat.AudioMediaFormat;
import org.schabi.newpipe.extractor.stream.mediaformat.SubtitlesMediaFormat;
import org.schabi.newpipe.extractor.stream.mediaformat.VideoMediaFormat;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.LocaleCompat;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MediaCCCStreamExtractor extends StreamExtractor {
    private JsonObject data;
    private JsonObject conferenceData;

    public MediaCCCStreamExtractor(final StreamingService service, final LinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Nullable
    @Override
    public String getTextualUploadDate() {
        return data.getString("release_date");
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return DateWrapper.fromOffsetDateTime(getTextualUploadDate());
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() {
        return getThumbnailsFromStreamItem(data);
    }

    @Nonnull
    @Override
    public Description getDescription() {
        return Description.of(data.getString("description"), Description.Type.PLAIN_TEXT);
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
    public List<Image> getUploaderAvatars() {
        return getImageListFromLogoImageUrl(conferenceData.getString("logo_url"));
    }

    @Override
    public List<AudioStream> getAudioStreams() throws ExtractionException {
        return List.of();
    }

    @Override
    public List<VideoStream> getVideoStreams() throws ExtractionException {
        return List.of();
    }

    @Override
    public List<VideoStream> getVideoOnlyStreams() {
        return List.of();
    }

    @Nonnull
    @Override
    public List<Stream> getStreams() throws IOException, ParsingException {
        final JsonArray recordings = data.getArray("recordings");
        final List<Stream> streams = new ArrayList<>(recordings.size());

        recordings.streamAsJsonObjects()
                .forEach(recording -> {
                    final String recordingUrl = recording.getString("recording_url");
                    if (Utils.isNullOrEmpty(recordingUrl) || recording.getString("folder", "")
                            .contains("slides")) {
                        // If this stream doesn't have a valid URL, we cannot add this stream
                        // Ignore video slides, as they are not the video content itself
                        return;
                    }

                    final String filename = recording.getString("filename");
                    final String mimeType = recording.getString("mime_type", "");
                    if (mimeType.startsWith("video/mp4")) {
                        // With MP4, audio streams should be encoded as AAC/M4A according to tests
                        // on some conferences
                        streams.add(buildMuxedVideoStream(recording, filename, recordingUrl,
                                VideoMediaFormat.MP4, AudioMediaFormat.M4A));
                    } else if (mimeType.startsWith("video/webm")) {
                        // With WEBM, audio streams should be encoded as Vorbis or Opus according
                        // to tests on some conferences
                        // As they are provided in a WEBM container with the video stream, they
                        // should be provided as WEBM too
                        streams.add(buildMuxedVideoStream(recording, filename, recordingUrl,
                                VideoMediaFormat.WEBM, AudioMediaFormat.WEBM));
                    } else if (mimeType.startsWith("audio/mpeg")) {
                        streams.add(buildAudioStream(recording, filename, recordingUrl,
                                AudioMediaFormat.MP3));
                    } else if (mimeType.startsWith("audio/ogg")) {
                        streams.add(buildAudioStream(recording, filename, recordingUrl,
                                AudioMediaFormat.OGG));
                    } else if (mimeType.startsWith("audio/opus")) {
                        streams.add(buildAudioStream(recording, filename, recordingUrl,
                                AudioMediaFormat.OPUS));
                    } else if (mimeType.startsWith("application/x-subrip")) {
                        streams.add(buildSubtitlesStream(recording, filename, recordingUrl,
                                SubtitlesMediaFormat.SRT));
                    } else if (mimeType.startsWith("text/vtt")) {
                        streams.add(buildSubtitlesStream(recording, filename, recordingUrl,
                                SubtitlesMediaFormat.WEBVTT));
                    }

                    // Ignore other unsupported formats
                });

        return streams;
    }

    @Nonnull
    private UriMuxedStream buildMuxedVideoStream(@Nonnull final JsonObject recording,
                                                 @Nullable final String filename,
                                                 @Nonnull final String url,
                                                 @Nonnull final VideoMediaFormat videoMediaFormat,
                                                 @Nonnull final AudioMediaFormat audioMediaFormat) {
        // Video streams may have one or multiple audio tracks in different languages
        final String languageString = recording.getString("language");
        final List<BaseAudioStream> demuxedAudioStreams;
        if (Utils.isNullOrEmpty(languageString)) {
            // Case that should not happen, as the language(s) should be always provided: one video
            // stream with one or multiple audio tracks
            demuxedAudioStreams = null;
        } else {
            demuxedAudioStreams = Arrays.stream(languageString.split("-"))
                    .map(LocaleCompat::forLanguageTag)
                    // We don't have info on what language is the original in the case of multiple
                    // tracks, so return a null audio track type
                    .map(locale -> new BaseAudioStreamImpl(null, null, locale.orElse(null),
                            Boolean.FALSE, audioMediaFormat, null, null, null, null, Boolean.FALSE,
                            null))
                    .collect(Collectors.toUnmodifiableList());
        }

        Integer width = recording.getInt("width", -1);
        if (width == -1) {
            width = null;
        }

        Integer height = recording.getInt("height", -1);
        if (height == -1) {
            height = null;
        }

        // We don't have info on what language is the original in the case of multiple
        // tracks, so return a null locale as the video one
        return new UriMuxedStream(filename, filename, demuxedAudioStreams, List.of(
                new BaseVideoStreamImpl(null, null, null, Boolean.FALSE, videoMediaFormat,
                        // There should be only 2D videos with a rectangular projection
                        BaseVideoStream.ProjectionType.RECTANGULAR, null, width, height, null, null,
                Boolean.FALSE)), List.of(), List.of(), null, new HttpDeliverySource(new UriObject(
                        url, UriObject.EXPIRATION_TIMESTAMP_NO_EXPIRY, null), List.of(), Map.of(),
                HttpDeliverySource.HttpMethod.GET, null), StreamingProtocol.PROGRESSIVE);
    }

    @Nonnull
    private UriAudioStream buildAudioStream(@Nonnull final JsonObject recording,
                                            @Nullable final String filename,
                                            @Nonnull final String url,
                                            @Nonnull final AudioMediaFormat audioMediaFormat) {
        // Audio streams are only in one language, we don't have info on what language is the
        // original in the case of multiple tracks, so return a null audio track type
        return new UriAudioStream(filename, filename, LocaleCompat.forLanguageTag(
                recording.getString("language")).orElse(null), Boolean.FALSE, audioMediaFormat,
                null, null, null, null, null, null, new HttpDeliverySource(new UriObject(url,
                UriObject.EXPIRATION_TIMESTAMP_NO_EXPIRY, null), List.of(), Map.of(),
                HttpDeliverySource.HttpMethod.GET, null), StreamingProtocol.PROGRESSIVE);
    }

    @Nonnull
    private UriSubtitlesStream buildSubtitlesStream(
            @Nonnull final JsonObject recording,
            @Nullable final String filename,
            @Nonnull final String url,
            @Nonnull final SubtitlesMediaFormat subtitlesMediaFormat) {
        // Subtitles streams are only in one language
        return new UriSubtitlesStream(filename, filename, LocaleCompat.forLanguageTag(
                recording.getString("language")).orElse(null), Boolean.FALSE, subtitlesMediaFormat,
                List.of(), new HttpDeliverySource(new UriObject(url,
                UriObject.EXPIRATION_TIMESTAMP_NO_EXPIRY, null), List.of(), Map.of(),
                HttpDeliverySource.HttpMethod.GET, null), StreamingProtocol.PROGRESSIVE);
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
            throw new ExtractionException("Could not parse json returned by URL: " + videoUrl,
                    jpe);
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
