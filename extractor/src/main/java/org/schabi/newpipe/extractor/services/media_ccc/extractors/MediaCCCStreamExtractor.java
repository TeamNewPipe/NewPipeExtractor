package org.schabi.newpipe.extractor.services.media_ccc.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCConferenceLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.*;
import org.schabi.newpipe.extractor.utils.JsonUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.schabi.newpipe.extractor.stream.AudioStream.UNKNOWN_BITRATE;

public class MediaCCCStreamExtractor extends StreamExtractor {
    private JsonObject data;
    private JsonObject conferenceData;

    private final List<AudioStream> audioStreams = new ArrayList<>();
    private final List<VideoStream> videoStreams = new ArrayList<>();

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
        if (audioStreams.isEmpty()) {
            final JsonArray recordings = data.getArray("recordings");
            for (int i = 0; i < recordings.size(); i++) {
                final JsonObject recording = recordings.getObject(i);
                final String mimeType = recording.getString("mime_type");
                if (mimeType.startsWith("audio")) {
                    // First we need to resolve the actual video data from CDN
                    final MediaFormat mediaFormat;
                    if (mimeType.endsWith("opus")) {
                        mediaFormat = MediaFormat.OPUS;
                    } else if (mimeType.endsWith("mpeg")) {
                        mediaFormat = MediaFormat.MP3;
                    } else if (mimeType.endsWith("ogg")) {
                        mediaFormat = MediaFormat.OGG;
                    } else {
                        mediaFormat = null;
                    }

                    // Don't use the containsSimilarStream method because it will always return
                    // false so if there are multiples audio streams available, only the first will
                    // be extracted in this case.
                    audioStreams.add(new AudioStream(recording.getString("filename"),
                            recording.getString("recording_url"), mediaFormat, UNKNOWN_BITRATE));
                }
            }
        }
        return audioStreams;
    }

    @Override
    public List<VideoStream> getVideoStreams() throws ExtractionException {
        if (videoStreams.isEmpty()) {
            final JsonArray recordings = data.getArray("recordings");
            for (int i = 0; i < recordings.size(); i++) {
                final JsonObject recording = recordings.getObject(i);
                final String mimeType = recording.getString("mime_type");
                if (mimeType.startsWith("video")) {
                    // First we need to resolve the actual video data from CDN

                    final MediaFormat mediaFormat;
                    if (mimeType.endsWith("webm")) {
                        mediaFormat = MediaFormat.WEBM;
                    } else if (mimeType.endsWith("mp4")) {
                        mediaFormat = MediaFormat.MPEG_4;
                    } else {
                        mediaFormat = null;
                    }

                    // Don't use the containsSimilarStream method because it will remove the
                    // extraction of some video versions (mostly languages)
                    videoStreams.add(new VideoStream(recording.getString("filename"),
                            recording.getString("recording_url"), mediaFormat,
                            recording.getInt("height") + "p", false));
                }
            }
        }
        return videoStreams;
    }

    @Override
    public List<VideoStream> getVideoOnlyStreams() {
        return Collections.emptyList();
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
            throw new ExtractionException("Could not parse json returned by url: " + videoUrl,
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
