package org.schabi.newpipe.extractor.services.media_ccc.extractors;

import static org.schabi.newpipe.extractor.stream.AudioStream.UNKNOWN_BITRATE;
import static org.schabi.newpipe.extractor.stream.Stream.ID_UNKNOWN;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.streamdata.delivery.DeliveryData;
import org.schabi.newpipe.extractor.streamdata.delivery.simpleimpl.SimpleDASHUrlDeliveryDataImpl;
import org.schabi.newpipe.extractor.streamdata.delivery.simpleimpl.SimpleHLSDeliveryDataImpl;
import org.schabi.newpipe.extractor.streamdata.delivery.simpleimpl.SimpleProgressiveHTTPDeliveryDataImpl;
import org.schabi.newpipe.extractor.streamdata.format.registry.AudioFormatRegistry;
import org.schabi.newpipe.extractor.streamdata.format.registry.VideoAudioFormatRegistry;
import org.schabi.newpipe.extractor.streamdata.stream.AudioStream;
import org.schabi.newpipe.extractor.streamdata.stream.VideoAudioStream;
import org.schabi.newpipe.extractor.streamdata.stream.quality.VideoQualityData;
import org.schabi.newpipe.extractor.streamdata.stream.simpleimpl.SimpleAudioStreamImpl;
import org.schabi.newpipe.extractor.streamdata.stream.simpleimpl.SimpleVideoAudioStreamImpl;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        final JsonArray doc = MediaCCCParsingHelper.getLiveStreams(
                downloader,
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
    public String getHlsMasterPlaylistUrl() {
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
        return getStreamDTOs("audio")
                .map(dto -> {
                    final String url = dto.getUrlValue().getString(URL);
                    final DeliveryData deliveryData;
                    if ("hls".equals(dto.getUrlKey())) {
                        deliveryData = new SimpleHLSDeliveryDataImpl(url);
                    } else if ("dash".equals(dto.getUrlKey())) {
                        deliveryData = new SimpleDASHUrlDeliveryDataImpl(url);
                    } else {
                        deliveryData = new SimpleProgressiveHTTPDeliveryDataImpl(url);
                    }

                    return new SimpleAudioStreamImpl(
                            deliveryData,
                            // TODO: This looks wrong
                            new AudioFormatRegistry().getFromSuffix(dto.getUrlKey())
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<VideoAudioStream> getVideoStreams() throws IOException, ExtractionException {
        return getStreamDTOs("video")
                .map(dto -> {
                    final String url = dto.getUrlValue().getString(URL);
                    final DeliveryData deliveryData;
                    if ("hls".equals(dto.getUrlKey())) {
                        deliveryData = new SimpleHLSDeliveryDataImpl(url);
                    } else if ("dash".equals(dto.getUrlKey())) {
                        deliveryData = new SimpleDASHUrlDeliveryDataImpl(url);
                    } else {
                        deliveryData = new SimpleProgressiveHTTPDeliveryDataImpl(url);
                    }

                    final JsonArray videoSize = dto.getStreamJsonObj().getArray("videoSize");

                    return new SimpleVideoAudioStreamImpl(
                            deliveryData,
                            // TODO: This looks wrong
                            new VideoAudioFormatRegistry().getFromSuffix(dto.getUrlKey()),
                            VideoQualityData.fromHeightWidth(
                                    /*height=*/videoSize.getInt(1, VideoQualityData.UNKNOWN),
                                    /*width=*/videoSize.getInt(0, VideoQualityData.UNKNOWN))
                    );
                })
                .collect(Collectors.toList());
    }

    private Stream<MediaCCCLiveStreamMapperDTO> getStreamDTOs(@Nonnull final String streamType) {
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
                                (JsonObject) e.getValue())));
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        return StreamType.LIVE_STREAM;
    }

    @Override
    public boolean isLive() {
        return true;
    }

    @Nonnull
    @Override
    public String getCategory() {
        return group;
    }
}
