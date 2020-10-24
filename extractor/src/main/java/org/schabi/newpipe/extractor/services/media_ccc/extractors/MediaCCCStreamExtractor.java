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
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.SubtitlesStream;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCConferenceLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCStreamLinkHandlerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
    public int getAgeLimit() {
        return 0;
    }

    @Override
    public long getLength() {
        return data.getInt("length");
    }

    @Override
    public long getTimeStamp() {
        return 0;
    }

    @Override
    public long getViewCount() {
        return data.getInt("view_count");
    }

    @Override
    public long getLikeCount() {
        return -1;
    }

    @Override
    public long getDislikeCount() {
        return -1;
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

    @Nonnull
    @Override
    public String getSubChannelUrl() {
        return "";
    }

    @Nonnull
    @Override
    public String getSubChannelName() {
        return "";
    }

    @Nonnull
    @Override
    public String getSubChannelAvatarUrl() {
        return "";
    }

    @Nonnull
    @Override
    public String getDashMpdUrl() {
        return "";
    }

    @Nonnull
    @Override
    public String getHlsUrl() {
        return "";
    }

    @Override
    public List<AudioStream> getAudioStreams() throws ExtractionException {
        final JsonArray recordings = data.getArray("recordings");
        final List<AudioStream> audioStreams = new ArrayList<>();
        for (int i = 0; i < recordings.size(); i++) {
            final JsonObject recording = recordings.getObject(i);
            final String mimeType = recording.getString("mime_type");
            if (mimeType.startsWith("audio")) {
                //first we need to resolve the actual video data from CDN
                final MediaFormat mediaFormat;
                if (mimeType.endsWith("opus")) {
                    mediaFormat = MediaFormat.OPUS;
                } else if (mimeType.endsWith("mpeg")) {
                    mediaFormat = MediaFormat.MP3;
                } else if (mimeType.endsWith("ogg")) {
                    mediaFormat = MediaFormat.OGG;
                } else {
                    throw new ExtractionException("Unknown media format: " + mimeType);
                }

                audioStreams.add(new AudioStream(recording.getString("recording_url"),
                        mediaFormat, -1));
            }
        }
        return audioStreams;
    }

    @Override
    public List<VideoStream> getVideoStreams() throws ExtractionException {
        final JsonArray recordings = data.getArray("recordings");
        final List<VideoStream> videoStreams = new ArrayList<>();
        for (int i = 0; i < recordings.size(); i++) {
            final JsonObject recording = recordings.getObject(i);
            final String mimeType = recording.getString("mime_type");
            if (mimeType.startsWith("video")) {
                //first we need to resolve the actual video data from CDN

                final MediaFormat mediaFormat;
                if (mimeType.endsWith("webm")) {
                    mediaFormat = MediaFormat.WEBM;
                } else if (mimeType.endsWith("mp4")) {
                    mediaFormat = MediaFormat.MPEG_4;
                } else {
                    throw new ExtractionException("Unknown media format: " + mimeType);
                }

                videoStreams.add(new VideoStream(recording.getString("recording_url"),
                        mediaFormat, recording.getInt("height") + "p"));
            }
        }
        return videoStreams;
    }

    @Override
    public List<VideoStream> getVideoOnlyStreams() {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public List<SubtitlesStream> getSubtitlesDefault() {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public List<SubtitlesStream> getSubtitles(final MediaFormat format) {
        return Collections.emptyList();
    }

    @Override
    public StreamType getStreamType() {
        return StreamType.VIDEO_STREAM;
    }

    @Nullable
    @Override
    public StreamInfoItemsCollector getRelatedStreams() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        final String videoUrl = MediaCCCStreamLinkHandlerFactory.VIDEO_API_ENDPOINT + getId();
        try {
            data = JsonParser.object().from(downloader.get(videoUrl).responseBody());
            conferenceData = JsonParser.object()
                    .from(downloader.get(data.getString("conference_url")).responseBody());
        } catch (JsonParserException jpe) {
            throw new ExtractionException("Could not parse json returned by url: " + videoUrl, jpe);
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

    @Nonnull
    @Override
    public String getHost() {
        return "";
    }

    @Nonnull
    @Override
    public String getPrivacy() {
        return "";
    }

    @Nonnull
    @Override
    public String getCategory() {
        return "";
    }

    @Nonnull
    @Override
    public String getLicence() {
        return "";
    }

    @Override
    public Locale getLanguageInfo() {
        return null;
    }

    @Nonnull
    @Override
    public List<String> getTags() {
        return Arrays.asList(data.getArray("tags").toArray(new String[0]));
    }

    @Nonnull
    @Override
    public String getSupportInfo() {
        return "";
    }
}
