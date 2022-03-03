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
import org.schabi.newpipe.extractor.stream.DeliveryMethod;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.VideoStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

import static org.schabi.newpipe.extractor.stream.AudioStream.UNKNOWN_BITRATE;
import static org.schabi.newpipe.extractor.stream.Stream.ID_UNKNOWN;
import static org.schabi.newpipe.extractor.utils.Utils.EMPTY_STRING;

public class MediaCCCLiveStreamExtractor extends StreamExtractor {
    private static final String STREAMS = "streams";
    private static final String URLS = "urls";
    private static final String URL = "url";

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

    /**
     * Get the URL of the first DASH stream found.
     *
     * <p>
     * There can be several DASH streams, so the URL of the first found is returned by this method.
     * </p>
     *
     * <p>
     * You can find the other video DASH streams by using {@link #getVideoStreams()}
     * </p>
     */
    @Nonnull
    @Override
    public String getDashMpdUrl() throws ParsingException {

        for (int s = 0; s < room.getArray(STREAMS).size(); s++) {
            final JsonObject stream = room.getArray(STREAMS).getObject(s);
            final JsonObject urls = stream.getObject(URLS);
            if (urls.has("dash")) {
                return urls.getObject("dash").getString(URL, EMPTY_STRING);
            }
        }

        return EMPTY_STRING;
    }

    /**
     * Get the URL of the first HLS stream found.
     *
     * <p>
     * There can be several HLS streams, so the URL of the first found is returned by this method.
     * </p>
     *
     * <p>
     * You can find the other video HLS streams by using {@link #getVideoStreams()}
     * </p>
     */
    @Nonnull
    @Override
    public String getHlsUrl() {
        for (int s = 0; s < room.getArray(STREAMS).size(); s++) {
            final JsonObject stream = room.getArray(STREAMS).getObject(s);
            final JsonObject urls = stream.getObject(URLS);
            if (urls.has("hls")) {
                return urls.getObject("hls").getString(URL, EMPTY_STRING);
            }
        }
        return EMPTY_STRING;
    }

    @Override
    public List<AudioStream> getAudioStreams() throws IOException, ExtractionException {
        final List<AudioStream> audioStreams = new ArrayList<>();
        IntStream.range(0, room.getArray(STREAMS).size())
                .mapToObj(s -> room.getArray(STREAMS).getObject(s))
                .filter(streamJsonObject -> streamJsonObject.getString("type").equals("audio"))
                .forEachOrdered(streamJsonObject -> streamJsonObject.getObject(URLS).keySet()
                        .forEach(type -> {
                            final JsonObject urlObject = streamJsonObject.getObject(URLS)
                                    .getObject(type);
                            // The DASH manifest will be extracted with getDashMpdUrl
                            if (!type.equals("dash")) {
                                final AudioStream.Builder builder = new AudioStream.Builder()
                                        .setId(urlObject.getString("tech", ID_UNKNOWN))
                                        .setContent(urlObject.getString(URL), true)
                                        .setAverageBitrate(UNKNOWN_BITRATE);
                                if (type.equals("hls")) {
                                    // We don't know with the type string what media format will
                                    // have HLS streams.
                                    // However, the tech string may contain some information
                                    // about the media format used.
                                    builder.setDeliveryMethod(DeliveryMethod.HLS);
                                } else {
                                    builder.setMediaFormat(MediaFormat.getFromSuffix(type));
                                }

                                audioStreams.add(builder.build());
                            }
                        }));

        return audioStreams;
    }

    @Override
    public List<VideoStream> getVideoStreams() throws IOException, ExtractionException {
        final List<VideoStream> videoStreams = new ArrayList<>();
        IntStream.range(0, room.getArray(STREAMS).size())
                .mapToObj(s -> room.getArray(STREAMS).getObject(s))
                .filter(stream -> stream.getString("type").equals("video"))
                .forEachOrdered(streamJsonObject -> streamJsonObject.getObject(URLS).keySet()
                        .forEach(type -> {
                            final String resolution =
                                    streamJsonObject.getArray("videoSize").getInt(0)
                                    + "x"
                                    + streamJsonObject.getArray("videoSize").getInt(1);
                            final JsonObject urlObject = streamJsonObject.getObject(URLS)
                                    .getObject(type);
                            // The DASH manifest will be extracted with getDashMpdUrl
                            if (!type.equals("dash")) {
                                final VideoStream.Builder builder = new VideoStream.Builder()
                                        .setId(urlObject.getString("tech", ID_UNKNOWN))
                                        .setContent(urlObject.getString(URL), true)
                                        .setIsVideoOnly(false)
                                        .setResolution(resolution);

                                if (type.equals("hls")) {
                                    // We don't know with the type string what media format will
                                    // have HLS streams.
                                    // However, the tech string may contain some information
                                    // about the media format used.
                                    builder.setDeliveryMethod(DeliveryMethod.HLS);
                                } else {
                                    builder.setMediaFormat(MediaFormat.getFromSuffix(type));
                                }

                                videoStreams.add(builder.build());
                            }
                        }));

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
