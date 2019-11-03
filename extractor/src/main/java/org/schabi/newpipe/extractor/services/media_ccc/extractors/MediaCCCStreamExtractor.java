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
import org.schabi.newpipe.extractor.stream.*;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MediaCCCStreamExtractor extends StreamExtractor {

    private JsonObject data;
    private JsonObject conferenceData;

    public MediaCCCStreamExtractor(StreamingService service, LinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Nonnull
    @Override
    public String getTextualUploadDate() throws ParsingException {
        return data.getString("release_date");
    }

    @Nonnull
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return new DateWrapper(MediaCCCParsingHelper.parseDateFrom(getTextualUploadDate()));
    }

    @Nonnull
    @Override
    public String getThumbnailUrl() throws ParsingException {
        return data.getString("thumb_url");
    }

    @Nonnull
    @Override
    public String getDescription() throws ParsingException {
        return data.getString("description");
    }

    @Override
    public int getAgeLimit() throws ParsingException {
        return 0;
    }

    @Override
    public long getLength() throws ParsingException {
        return data.getInt("length");
    }

    @Override
    public long getTimeStamp() throws ParsingException {
        return 0;
    }

    @Override
    public long getViewCount() throws ParsingException {
        return data.getInt("view_count");
    }

    @Override
    public long getLikeCount() throws ParsingException {
        return -1;
    }

    @Override
    public long getDislikeCount() throws ParsingException {
        return -1;
    }

    @Nonnull
    @Override
    public String getUploaderUrl() throws ParsingException {
        return data.getString("conference_url");
    }

    @Nonnull
    @Override
    public String getUploaderName() throws ParsingException {
        return data.getString("conference_url")
                .replace("https://api.media.ccc.de/public/conferences/", "");
    }

    @Nonnull
    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        return conferenceData.getString("logo_url");
    }

    @Nonnull
    @Override
    public String getDashMpdUrl() throws ParsingException {
        return null;
    }

    @Nonnull
    @Override
    public String getHlsUrl() throws ParsingException {
        return null;
    }

    @Override
    public List<AudioStream> getAudioStreams() throws IOException, ExtractionException {
        final JsonArray recordings = data.getArray("recordings");
        final List<AudioStream> audioStreams = new ArrayList<>();
        for(int i = 0; i < recordings.size(); i++) {
            final JsonObject recording = recordings.getObject(i);
            final String mimeType = recording.getString("mime_type");
            if(mimeType.startsWith("audio")) {
                //first we need to resolve the actual video data from CDN
                final MediaFormat mediaFormat;
                if(mimeType.endsWith("opus")) {
                    mediaFormat = MediaFormat.OPUS;
                } else if(mimeType.endsWith("mpeg")) {
                    mediaFormat = MediaFormat.MP3;
                } else if(mimeType.endsWith("ogg")){
                    mediaFormat = MediaFormat.OGG;
                } else {
                    throw new ExtractionException("Unknown media format: " + mimeType);
                }

                audioStreams.add(new AudioStream(recording.getString("recording_url"), mediaFormat, -1));
            }
        }
        return audioStreams;
    }

    @Override
    public List<VideoStream> getVideoStreams() throws IOException, ExtractionException {
        final JsonArray recordings = data.getArray("recordings");
        final List<VideoStream> videoStreams = new ArrayList<>();
        for(int i = 0; i < recordings.size(); i++) {
            final JsonObject recording = recordings.getObject(i);
            final String mimeType = recording.getString("mime_type");
            if(mimeType.startsWith("video")) {
                //first we need to resolve the actual video data from CDN

                final MediaFormat mediaFormat;
                if(mimeType.endsWith("webm")) {
                    mediaFormat = MediaFormat.WEBM;
                } else if(mimeType.endsWith("mp4")) {
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
    public List<VideoStream> getVideoOnlyStreams() throws IOException, ExtractionException {
        return null;
    }

    @Nonnull
    @Override
    public List<SubtitlesStream> getSubtitlesDefault() throws IOException, ExtractionException {
        return null;
    }

    @Nonnull
    @Override
    public List<SubtitlesStream> getSubtitles(MediaFormat format) throws IOException, ExtractionException {
        return null;
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public StreamInfoItem getNextStream() throws IOException, ExtractionException {
        return null;
    }

    @Override
    public StreamInfoItemsCollector getRelatedStreams() throws IOException, ExtractionException {
        return new StreamInfoItemsCollector(getServiceId());
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        try {
            data = JsonParser.object().from(
                    downloader.get(getLinkHandler().getUrl()).responseBody());
            conferenceData = JsonParser.object()
                    .from(downloader.get(getUploaderUrl()).responseBody());
        } catch (JsonParserException jpe) {
            throw new ExtractionException("Could not parse json returned by url: " + getLinkHandler().getUrl(), jpe);
        }

    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return data.getString("title");
    }

    @Nonnull
    @Override
    public String getOriginalUrl() throws ParsingException {
        return data.getString("frontend_link");
    }
}
