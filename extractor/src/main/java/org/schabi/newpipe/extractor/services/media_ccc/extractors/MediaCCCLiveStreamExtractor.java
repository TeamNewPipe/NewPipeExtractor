package org.schabi.newpipe.extractor.services.media_ccc.extractors;

import static org.schabi.newpipe.extractor.stream.AudioStream.UNKNOWN_BITRATE;
import static org.schabi.newpipe.extractor.stream.Stream.ID_UNKNOWN;

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
import org.schabi.newpipe.extractor.stream.Stream;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.VideoStream;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

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
                        conference = conferenceObject;
                        group = groupObject;
                        room = roomObject;
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
     * There can be several DASH streams, so the URL of the first one found is returned by this
     * method.
     * </p>
     *
     * <p>
     * You can find the other DASH video streams by using {@link #getVideoStreams()}
     * </p>
     */
    @Nonnull
    @Override
    public String getDashMpdUrl() throws ParsingException {
        return getManifestOfDeliveryMethodWanted("dash");
    }

    /**
     * Get the URL of the first HLS stream found.
     *
     * <p>
     * There can be several HLS streams, so the URL of the first one found is returned by this
     * method.
     * </p>
     *
     * <p>
     * You can find the other HLS video streams by using {@link #getVideoStreams()}
     * </p>
     */
    @Nonnull
    @Override
    public String getHlsUrl() {
        return getManifestOfDeliveryMethodWanted("hls");
    }

    @Nonnull
    private String getManifestOfDeliveryMethodWanted(@Nonnull final String deliveryMethod) {
        return room.getArray(STREAMS).stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .map(streamObject -> streamObject.getObject(URLS))
                .filter(urls -> urls.has(deliveryMethod))
                .map(urls -> urls.getObject(deliveryMethod).getString(URL, ""))
                .findFirst()
                .orElse("");
    }

    @Override
    public List<AudioStream> getAudioStreams() throws IOException, ExtractionException {
        return getStreams("audio",
                dto -> {
                    final AudioStream.Builder builder = new AudioStream.Builder()
                            .setId(dto.urlValue.getString("tech", ID_UNKNOWN))
                            .setContent(dto.urlValue.getString(URL), true)
                            .setAverageBitrate(UNKNOWN_BITRATE);

                    if ("hls".equals(dto.urlKey)) {
                        // We don't know with the type string what media format will
                        // have HLS streams.
                        // However, the tech string may contain some information
                        // about the media format used.
                        return builder.setDeliveryMethod(DeliveryMethod.HLS)
                                .build();
                    }

                    return builder.setMediaFormat(MediaFormat.getFromSuffix(dto.urlKey))
                            .build();
                });
    }

    @Override
    public List<VideoStream> getVideoStreams() throws IOException, ExtractionException {
        return getStreams("video",
                dto -> {
                    final JsonArray videoSize = dto.streamJsonObj.getArray("videoSize");

                    final VideoStream.Builder builder = new VideoStream.Builder()
                            .setId(dto.urlValue.getString("tech", ID_UNKNOWN))
                            .setContent(dto.urlValue.getString(URL), true)
                            .setIsVideoOnly(false)
                            .setResolution(videoSize.getInt(0) + "x" + videoSize.getInt(1));

                    if ("hls".equals(dto.urlKey)) {
                        // We don't know with the type string what media format will
                        // have HLS streams.
                        // However, the tech string may contain some information
                        // about the media format used.
                        return builder.setDeliveryMethod(DeliveryMethod.HLS)
                                .build();
                    }

                    return builder.setMediaFormat(MediaFormat.getFromSuffix(dto.urlKey))
                            .build();
                });
    }


    /**
     * This is just an internal class used in {@link #getStreams(String, Function)} to tie together
     * the stream json object, its URL key and its URL value. An object of this class would be
     * temporary and the three values it holds would be <b>convert</b>ed to a proper {@link Stream}
     * object based on the wanted stream type.
     */
    private static final class MediaCCCLiveStreamMapperDTO {
        final JsonObject streamJsonObj;
        final String urlKey;
        final JsonObject urlValue;

        MediaCCCLiveStreamMapperDTO(final JsonObject streamJsonObj,
                                    final String urlKey,
                                    final JsonObject urlValue) {
            this.streamJsonObj = streamJsonObj;
            this.urlKey = urlKey;
            this.urlValue = urlValue;
        }
    }

    private <T extends Stream> List<T> getStreams(
            @Nonnull final String streamType,
            @Nonnull final Function<MediaCCCLiveStreamMapperDTO, T> converter) {
        return room.getArray(STREAMS).stream()
                // Ensure that we use only process JsonObjects
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                // Only process streams of requested type
                .filter(streamJsonObj -> streamType.equals(streamJsonObj.getString("type")))
                // Flatmap Urls and ensure that we use only process JsonObjects
                .flatMap(streamJsonObj -> streamJsonObj.getObject(URLS).entrySet().stream()
                        .filter(e -> e.getValue() instanceof JsonObject)
                        .map(e -> new MediaCCCLiveStreamMapperDTO(
                                streamJsonObj,
                                e.getKey(),
                                (JsonObject) e.getValue())))
                // The DASH manifest will be extracted with getDashMpdUrl
                .filter(dto -> !"dash".equals(dto.urlKey))
                // Convert
                .map(converter)
                .collect(Collectors.toList());
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
