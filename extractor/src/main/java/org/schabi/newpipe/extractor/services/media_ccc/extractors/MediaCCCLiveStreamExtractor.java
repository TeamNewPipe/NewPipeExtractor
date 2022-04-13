package org.schabi.newpipe.extractor.services.media_ccc.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.VideoStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class MediaCCCLiveStreamExtractor extends StreamExtractor {
    private JsonObject conference = null;
    private String group = "";
    private JsonObject room = null;

    public MediaCCCLiveStreamExtractor(final StreamingService service,
                                       final LinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        final JsonArray doc =
                MediaCCCParsingHelper.getLiveStreams(downloader, getExtractorLocalization());
        // find correct room
        for (int c = 0; c < doc.size(); c++) {
            conference = doc.getObject(c);
            final JsonArray groups = conference.getArray("groups");
            for (int g = 0; g < groups.size(); g++) {
                group = groups.getObject(g).getString("group");
                final JsonArray rooms = groups.getObject(g).getArray("rooms");
                for (int r = 0; r < rooms.size(); r++) {
                    room = rooms.getObject(r);
                    if (getId().equals(
                            conference.getString("slug") + "/" + room.getString("slug"))) {
                        return;
                    }
                }
            }
        }
        throw new ExtractionException("Could not find room matching id: '" + getId() + "'");
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return room.getString("display");
    }

    @Nonnull
    @Override
    public String getThumbnailUrl() throws ParsingException {
        return room.getString("thumb");
    }

    @Nonnull
    @Override
    public Description getDescription() throws ParsingException {
        return new Description(conference.getString("description")
                + " - " + group, Description.PLAIN_TEXT);
    }

    @Override
    public long getViewCount() {
        return -1;
    }

    @Nonnull
    @Override
    public String getUploaderUrl() throws ParsingException {
        return "https://streaming.media.ccc.de/" + conference.getString("slug");
    }

    @Nonnull
    @Override
    public String getUploaderName() throws ParsingException {
        return conference.getString("conference");
    }

    @Nonnull
    @Override
    public String getHlsUrl() {
        // TODO: There are multiple HLS streams.
        //       Make getHlsUrl() and getDashMpdUrl() return lists of VideoStreams,
        //       so the user can choose a resolution.
        for (int s = 0; s < room.getArray("streams").size(); s++) {
            final JsonObject stream = room.getArray("streams").getObject(s);
            if (stream.getString("type").equals("video")) {
                if (stream.has("hls")) {
                    return stream.getObject("urls").getObject("hls").getString("url");
                }
            }
        }
        return "";
    }

    @Override
    public List<AudioStream> getAudioStreams() throws IOException, ExtractionException {
        final List<AudioStream> audioStreams = new ArrayList<>();
        for (int s = 0; s < room.getArray("streams").size(); s++) {
            final JsonObject stream = room.getArray("streams").getObject(s);
            if (stream.getString("type").equals("audio")) {
                for (final String type : stream.getObject("urls").keySet()) {
                    final JsonObject url = stream.getObject("urls").getObject(type);
                    audioStreams.add(new AudioStream(url.getString("url"),
                            MediaFormat.getFromSuffix(type), -1));
                }
            }
        }
        return audioStreams;
    }

    @Override
    public List<VideoStream> getVideoStreams() throws IOException, ExtractionException {
        final List<VideoStream> videoStreams = new ArrayList<>();
        for (int s = 0; s < room.getArray("streams").size(); s++) {
            final JsonObject stream = room.getArray("streams").getObject(s);
            if (stream.getString("type").equals("video")) {
                final String resolution = stream.getArray("videoSize").getInt(0) + "x"
                        + stream.getArray("videoSize").getInt(1);
                for (final String type : stream.getObject("urls").keySet()) {
                    if (!type.equals("hls")) {
                        final JsonObject url = stream.getObject("urls").getObject(type);
                        videoStreams.add(new VideoStream(
                                url.getString("url"),
                                MediaFormat.getFromSuffix(type),
                                resolution));
                    }
                }
            }
        }
        return videoStreams;
    }

    @Override
    public List<VideoStream> getVideoOnlyStreams() {
        return null;
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        return StreamType.LIVE_STREAM; // TODO: video and audio only streams are both available
    }

    @Nonnull
    @Override
    public String getCategory() {
        return group;
    }
}
