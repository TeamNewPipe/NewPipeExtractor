package org.schabi.newpipe.extractor.services.media_ccc.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.stream.*;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.schabi.newpipe.extractor.stream.AudioStream.UNKNOWN_BITRATE;
import static org.schabi.newpipe.extractor.utils.Utils.EMPTY_STRING;

public class MediaCCCLiveStreamExtractor extends StreamExtractor {
    private JsonObject conference = null;
    private String group = "";
    private JsonObject room = null;

    private final List<AudioStream> audioStreams = new ArrayList<>();
    private final List<VideoStream> videoStreams = new ArrayList<>();
    private String firstDashUrlFound = null;
    private String firstHlsUrlFound = null;

    public MediaCCCLiveStreamExtractor(final StreamingService service,
                                       final LinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        final JsonArray doc = MediaCCCParsingHelper.getLiveStreams(downloader,
                getExtractorLocalization());
        // Find the correct room
        for (int c = 0; c < doc.size(); c++) {
            final JsonObject conferenceObject = doc.getObject(c);
            final JsonArray groups = conferenceObject.getArray("groups");
            for (int g = 0; g < groups.size(); g++) {
                final String groupObject = groups.getObject(g).getString("group");
                final JsonArray rooms = groups.getObject(g).getArray("rooms");
                for (int r = 0; r < rooms.size(); r++) {
                    final JsonObject roomObject = rooms.getObject(r);
                    if (getId().equals(conferenceObject.getString("slug") + "/"
                            + roomObject.getString("slug"))) {
                        this.conference = conferenceObject;
                        this.group = groupObject;
                        this.room = roomObject;
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
        return new Description(conference.getString("description") + " - " + group,
                Description.PLAIN_TEXT);
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

    /**
     * Get the URL of the first DASH stream found.
     * <p>
     * There can be several DASH streams, so the URL of the first found is returned by this method.
     * <br>
     * You can find the other video DASH streams by using {@link #getVideoStreams()}
     * </p>
     */
    @Nonnull
    @Override
    public String getDashMpdUrl() throws ParsingException {
        if (firstDashUrlFound == null) {
            for (int s = 0; s < room.getArray("streams").size(); s++) {
                final JsonObject stream = room.getArray("streams").getObject(s);
                final JsonObject urls = stream.getObject("urls");
                if (urls.has("dash")) {
                    firstDashUrlFound = urls.getObject("dash").getString("url", EMPTY_STRING);
                    return firstDashUrlFound;
                }
            }
            firstDashUrlFound = EMPTY_STRING;
        }
        return firstDashUrlFound;
    }

    /**
     * Get the URL of the first HLS stream found.
     * <p>
     * There can be several HLS streams, so the URL of the first found is returned by this method.
     * <br>
     * You can find the other video HLS streams by using {@link #getVideoStreams()}
     * </p>
     */
    @Nonnull
    @Override
    public String getHlsUrl() {
        if (firstHlsUrlFound == null) {
            for (int s = 0; s < room.getArray("streams").size(); s++) {
                final JsonObject stream = room.getArray("streams").getObject(s);
                final JsonObject urls = stream.getObject("urls");
                if (urls.has("hls")) {
                    firstHlsUrlFound = urls.getObject("hls").getString("url", EMPTY_STRING);
                    return firstHlsUrlFound;
                }
            }
            firstHlsUrlFound = EMPTY_STRING;
        }
        return firstHlsUrlFound;
    }

    @Override
    public List<AudioStream> getAudioStreams() throws IOException, ExtractionException {
        if (audioStreams.isEmpty()) {
            for (int s = 0; s < room.getArray("streams").size(); s++) {
                final JsonObject stream = room.getArray("streams").getObject(s);
                if (stream.getString("type").equals("audio")) {
                    for (final String type : stream.getObject("urls").keySet()) {
                        final JsonObject urlObject = stream.getObject("urls").getObject(type);
                        // The DASH manifest will be extracted with getDashMpdUrl
                        if (!type.equals("dash")) {
                            if (type.equals("hls")) {
                                audioStreams.add(new AudioStream(urlObject.getString("tech"),
                                        urlObject.getString("url"),
                                        true,
                                        // We don't know with the type string what media format
                                        // will have HLS streams.
                                        // However, the tech string may contain some information
                                        // about the media format used.
                                        null,
                                        DeliveryMethod.HLS,
                                        UNKNOWN_BITRATE));
                            } else {
                                audioStreams.add(new AudioStream(urlObject.getString("tech"),
                                        urlObject.getString("url"),
                                        MediaFormat.getFromSuffix(type),
                                        UNKNOWN_BITRATE));
                            }
                        }
                    }
                }
            }
        }
        return audioStreams;
    }

    @Override
    public List<VideoStream> getVideoStreams() throws IOException, ExtractionException {
        if (videoStreams.isEmpty()) {
            for (int s = 0; s < room.getArray("streams").size(); s++) {
                final JsonObject stream = room.getArray("streams").getObject(s);
                if (stream.getString("type").equals("video")) {
                    final String resolution = stream.getArray("videoSize").getInt(0) + "x"
                            + stream.getArray("videoSize").getInt(1);
                    for (final String type : stream.getObject("urls").keySet()) {
                        final JsonObject urlObject = stream.getObject("urls").getObject(type);
                        // The DASH manifest will be extracted with getDashMpdUrl
                        if (!type.equals("dash")) {
                            if (type.equals("hls")) {
                                videoStreams.add(new VideoStream(urlObject.getString("tech"),
                                        urlObject.getString("url"),
                                        true,
                                        // We don't know with the type string what type will have
                                        // HLS
                                        // streams.
                                        // However, the tech string may contain some information
                                        // about
                                        // the media format used.
                                        null,
                                        DeliveryMethod.HLS,
                                        resolution,
                                        false,
                                        null));
                            } else {
                                videoStreams.add(new VideoStream(urlObject.getString("tech"),
                                        urlObject.getString("url"),
                                        MediaFormat.getFromSuffix(type),
                                        resolution,
                                        false));
                            }
                        }
                    }
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
    public StreamType getStreamType() throws ParsingException {
        return StreamType.LIVE_STREAM;
    }

    @Nonnull
    @Override
    public String getCategory() {
        return group;
    }
}
