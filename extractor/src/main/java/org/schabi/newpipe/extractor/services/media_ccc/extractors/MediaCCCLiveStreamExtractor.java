package org.schabi.newpipe.extractor.services.media_ccc.extractors;

import static org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCParsingHelper.getThumbnailsFromLiveStreamItem;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.StreamingProtocol;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.schabi.newpipe.extractor.stream.deliverysource.uri.HttpDeliverySource;
import org.schabi.newpipe.extractor.stream.deliverysource.uri.UriObject;
import org.schabi.newpipe.extractor.stream.impl.UriManifestStream;
import org.schabi.newpipe.extractor.stream.interfaces.Stream;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        final JsonArray doc = MediaCCCParsingHelper.getLiveStreams(downloader,
                getExtractorLocalization());
        final String id = getId();

        // Find the correct room
        for (int c = 0; c < doc.size(); c++) {
            final JsonObject conferenceObject = doc.getObject(c);
            final JsonArray groups = conferenceObject.getArray("groups");
            for (int g = 0; g < groups.size(); g++) {
                final String groupObject = groups.getObject(g).getString("group");
                final JsonArray rooms = groups.getObject(g).getArray("rooms");
                for (int r = 0; r < rooms.size(); r++) {
                    final JsonObject roomObject = rooms.getObject(r);
                    if (id.equals(conferenceObject.getString("mandator") + "/"
                            + roomObject.getString("slug"))) {
                        conference = conferenceObject;
                        group = groupObject;
                        room = roomObject;
                        return;
                    }
                }
            }
        }

        throw new ExtractionException("Could not find room matching id: '" + id + "'");
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return room.getString("display");
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() throws ParsingException {
       return getThumbnailsFromLiveStreamItem(room);
    }

    @Nonnull
    @Override
    public Description getDescription() throws ParsingException {
        final String text = conference.getString("description") + " - " + group;
        return new Description(text, Description.Type.PLAIN_TEXT);
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

    @Override
    public List<AudioStream> getAudioStreams() throws IOException, ExtractionException {
        return List.of();
    }

    @Override
    public List<VideoStream> getVideoStreams() throws IOException, ExtractionException {
        return List.of();
    }

    @Override
    public List<VideoStream> getVideoOnlyStreams() {
        return List.of();
    }

    @Nonnull
    @Override
    public List<Stream> getStreams() throws IOException, ParsingException {
        final List<Stream> streams = new ArrayList<>();

        room.getArray("streams")
                .streamAsJsonObjects()
                .map(stream -> stream.getObject("urls"))
                .filter(urlsArray -> !urlsArray.isEmpty())
                .forEach(urlsArray -> {
                    buildAndAddStreamIfAvailable(urlsArray, "hls",
                            StreamingProtocol.HLS, streams);
                    buildAndAddStreamIfAvailable(urlsArray, "dash",
                            StreamingProtocol.DASH, streams);
                });

        return streams;
    }

    private void buildAndAddStreamIfAvailable(@Nonnull final JsonObject urlsArray,
                                              @Nonnull final String streamingProtocolKey,
                                              @Nonnull final StreamingProtocol streamingProtocol,
                                              @Nonnull final List<Stream> streams) {
        if (urlsArray.has(streamingProtocolKey)) {
            final JsonObject streamingProtocolJson = urlsArray.getObject(streamingProtocolKey);
            final String url = streamingProtocolJson.getString("url");
            if (!Utils.isNullOrEmpty(url)) {
                streams.add(new UriManifestStream(streamingProtocolKey,
                        streamingProtocolJson.getString("display"),
                        new HttpDeliverySource(new UriObject(url,
                                UriObject.EXPIRATION_TIMESTAMP_UNKNOWN, null), List.of(),
                                Map.of(), HttpDeliverySource.HttpMethod.GET, null),
                        streamingProtocol));
            }
        }
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
