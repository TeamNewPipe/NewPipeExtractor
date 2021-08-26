package org.schabi.newpipe.extractor.services.media_ccc.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.MetaInfo;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.stream.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.schabi.newpipe.extractor.stream.AudioStream.UNKNOWN_BITRATE;
import static org.schabi.newpipe.extractor.utils.Utils.EMPTY_STRING;

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
        // Find correct room
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

    @Nullable
    @Override
    public String getTextualUploadDate() throws ParsingException {
        return null;
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return null;
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
    public int getAgeLimit() {
        return 0;
    }

    @Override
    public long getLength() {
        return 0;
    }

    @Override
    public long getTimeStamp() throws ParsingException {
        return 0;
    }

    @Override
    public long getViewCount() {
        return -1;
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
    public String getUploaderUrl() throws ParsingException {
        return "https://streaming.media.ccc.de/" + conference.getString("slug");
    }

    @Nonnull
    @Override
    public String getUploaderName() throws ParsingException {
        return conference.getString("conference");
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return false;
    }

    @Nonnull
    @Override
    public String getUploaderAvatarUrl() {
        return EMPTY_STRING;
    }

    @Nonnull
    @Override
    public String getSubChannelUrl() {
        return EMPTY_STRING;
    }

    @Nonnull
    @Override
    public String getSubChannelName() {
        return EMPTY_STRING;
    }

    @Nonnull
    @Override
    public String getSubChannelAvatarUrl() {
        return EMPTY_STRING;
    }

    @Nonnull
    @Override
    public String getDashMpdUrl() throws ParsingException {
        for (int s = 0; s < room.getArray("streams").size(); s++) {
            final JsonObject stream = room.getArray("streams").getObject(s);
            final JsonObject urls = stream.getObject("urls");
            if (urls.has("dash")) {
                return urls.getObject("dash").getString("url");
            }
        }
        return EMPTY_STRING;
    }

    /**
     * Get the URL of the first HLS stream found.
     * <p>
     * There can be several HLS streams, so the URL of the first found is returned by this method.
     * <br>
     * You can find the other video HLS streams
     * </p>
     */
    @Nonnull
    @Override
    public String getHlsUrl() {
        for (int s = 0; s < room.getArray("streams").size(); s++) {
            final JsonObject stream = room.getArray("streams").getObject(s);
            final JsonObject urls = stream.getObject("urls");
            if (urls.has("hls")) {
                return urls.getObject("hls").getString("url");
            }
        }
        return EMPTY_STRING;
    }

    @Override
    public List<AudioStream> getAudioStreams() throws IOException, ExtractionException {
        final List<AudioStream> audioStreams = new ArrayList<>();
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
                                    // We don't know with the type string what media format will
                                    // have HLS streams.
                                    // However, the tech string may contain some information about
                                    // the media format used.
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
                    final JsonObject urlObject = stream.getObject("urls").getObject(type);
                    // The DASH manifest will be extracted with getDashMpdUrl
                    if (!type.equals("dash")) {
                        if (type.equals("hls")) {
                            videoStreams.add(new VideoStream(urlObject.getString("tech"),
                                    urlObject.getString("url"),
                                    true,
                                    // We don't know with the type string what type will have HLS
                                    // streams.
                                    // However, the tech string may contain some information about
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
    public List<SubtitlesStream> getSubtitles(MediaFormat format) {
        return Collections.emptyList();
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        return StreamType.LIVE_STREAM; // TODO: video and audio only streams are both available
    }

    @Nullable
    @Override
    public StreamInfoItemsCollector getRelatedItems() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Nonnull
    @Override
    public String getHost() {
        return EMPTY_STRING;
    }

    @Nonnull
    @Override
    public Privacy getPrivacy() {
        return Privacy.PUBLIC;
    }

    @Nonnull
    @Override
    public String getCategory() {
        return group;
    }

    @Nonnull
    @Override
    public String getLicence() {
        return EMPTY_STRING;
    }

    @Nullable
    @Override
    public Locale getLanguageInfo() {
        return null;
    }

    @Nonnull
    @Override
    public List<String> getTags() {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public String getSupportInfo() {
        return EMPTY_STRING;
    }

    @Nonnull
    @Override
    public List<StreamSegment> getStreamSegments() {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public List<MetaInfo> getMetaInfo() {
        return Collections.emptyList();
    }
}
