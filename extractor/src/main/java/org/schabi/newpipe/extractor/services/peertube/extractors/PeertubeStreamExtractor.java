package org.schabi.newpipe.extractor.services.peertube.extractors;

import static org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper.getAvatarsFromOwnerAccountOrVideoChannelObject;
import static org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper.getThumbnailsFromPlaylistOrVideoItem;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.peertube.PeertubeParsingHelper;
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeSearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.services.peertube.linkHandler.PeertubeStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.AudioTrackType;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.Frameset;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamSegment;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.StreamingProtocol;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.schabi.newpipe.extractor.stream.deliverysource.uri.HttpDeliverySource;
import org.schabi.newpipe.extractor.stream.deliverysource.uri.UriObject;
import org.schabi.newpipe.extractor.stream.impl.UriAudioStream;
import org.schabi.newpipe.extractor.stream.impl.UriManifestStream;
import org.schabi.newpipe.extractor.stream.impl.UriMuxedStream;
import org.schabi.newpipe.extractor.stream.impl.UriSubtitlesStream;
import org.schabi.newpipe.extractor.stream.impl.UriVideoStream;
import org.schabi.newpipe.extractor.stream.impl.base.BaseAudioStreamImpl;
import org.schabi.newpipe.extractor.stream.impl.base.BaseVideoStreamImpl;
import org.schabi.newpipe.extractor.stream.interfaces.Stream;
import org.schabi.newpipe.extractor.stream.interfaces.SubtitlesStream;
import org.schabi.newpipe.extractor.stream.interfaces.base.BaseVideoStream;
import org.schabi.newpipe.extractor.stream.mediaformat.AudioMediaFormat;
import org.schabi.newpipe.extractor.stream.mediaformat.SubtitlesMediaFormat;
import org.schabi.newpipe.extractor.stream.mediaformat.VideoMediaFormat;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.LocaleCompat;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PeertubeStreamExtractor extends StreamExtractor {
    private static final String ACCOUNT_HOST = "account.host";
    private static final String ACCOUNT_NAME = "account.name";
    private static final String FILES = "files";
    private static final String FILE_DOWNLOAD_URL = "fileDownloadUrl";
    private static final String FILE_URL = "fileUrl";
    private static final String PLAYLIST_URL = "playlistUrl";
    private static final String STREAMING_PLAYLISTS = "streamingPlaylists";

    private final String baseUrl;
    private JsonObject json;

    private List<SubtitlesStream> cachedSubtitlesStreams;

    public PeertubeStreamExtractor(final StreamingService service, final LinkHandler linkHandler)
            throws ParsingException {
        super(service, linkHandler);
        this.baseUrl = getBaseUrl();
    }

    @Override
    public String getTextualUploadDate() throws ParsingException {
        return JsonUtils.getString(json, "publishedAt");
    }

    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return DateWrapper.fromInstant(getTextualUploadDate());
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() throws ParsingException {
        return getThumbnailsFromPlaylistOrVideoItem(baseUrl, json);
    }

    @Nonnull
    @Override
    public Description getDescription() throws ParsingException {
        String text;
        try {
            text = JsonUtils.getString(json, "description");
        } catch (final ParsingException e) {
            return Description.EMPTY_DESCRIPTION;
        }
        if (text.length() == 250 && text.substring(247).equals("...")) {
            // If description is shortened, get full description
            final Downloader dl = NewPipe.getDownloader();
            try {
                final Response response = dl.get(baseUrl
                        + PeertubeStreamLinkHandlerFactory.VIDEO_API_ENDPOINT
                        + getId() + "/description");
                final JsonObject jsonObject = JsonParser.object().from(response.responseBody());
                text = JsonUtils.getString(jsonObject, "description");
            } catch (final IOException | ReCaptchaException | JsonParserException ignored) {
                // Something went wrong when getting the full description, use the shortened one
            }
        }
        return Description.of(text, Description.Type.MARKDOWN);
    }

    @Override
    public int getAgeLimit() throws ParsingException {
        final boolean isNSFW = JsonUtils.getBoolean(json, "nsfw");
        if (isNSFW) {
            return 18;
        } else {
            return NO_AGE_LIMIT;
        }
    }

    @Override
    public long getLength() {
        return json.getLong("duration");
    }

    @Override
    public long getTimeStamp() throws ParsingException {
        final long timestamp = getTimestampSeconds(
                "((#|&|\\?)start=\\d{0,3}h?\\d{0,3}m?\\d{1,3}s?)");

        if (timestamp == -2) {
            // regex for timestamp was not found
            return 0;
        } else {
            return timestamp;
        }
    }

    @Override
    public long getViewCount() {
        return json.getLong("views");
    }

    @Override
    public long getLikeCount() {
        return json.getLong("likes");
    }

    @Override
    public long getDislikeCount() {
        return json.getLong("dislikes");
    }

    @Nonnull
    @Override
    public String getUploaderUrl() throws ParsingException {
        final String name = JsonUtils.getString(json, ACCOUNT_NAME);
        final String host = JsonUtils.getString(json, ACCOUNT_HOST);
        return getService().getChannelLHFactory().fromId("accounts/" + name + "@" + host, baseUrl)
                .getUrl();
    }

    @Nonnull
    @Override
    public String getUploaderName() throws ParsingException {
        return JsonUtils.getString(json, "account.displayName");
    }

    @Nonnull
    @Override
    public List<Image> getUploaderAvatars() {
        return getAvatarsFromOwnerAccountOrVideoChannelObject(baseUrl, json.getObject("account"));
    }

    @Nonnull
    @Override
    public String getSubChannelUrl() throws ParsingException {
        return JsonUtils.getString(json, "channel.url");
    }

    @Nonnull
    @Override
    public String getSubChannelName() throws ParsingException {
        return JsonUtils.getString(json, "channel.displayName");
    }

    @Nonnull
    @Override
    public List<Image> getSubChannelAvatars() {
        return getAvatarsFromOwnerAccountOrVideoChannelObject(baseUrl, json.getObject("channel"));
    }

    @Override
    public List<AudioStream> getAudioStreams() throws ParsingException {
        return List.of();
    }

    @Override
    public List<VideoStream> getVideoStreams() throws ExtractionException {
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

        // Add files streams (MP4 progressive only)
        getStreamsFromArray(json.getArray(FILES), streams);

        // Add streaming playlists streams (MP4 progressive fragmented and HLS playlists using
        // these streams)
        json.getArray(STREAMING_PLAYLISTS)
                .streamAsJsonObjects()
                .forEach(streamingPlaylist -> {
                    getStreamsFromArray(streamingPlaylist.getArray(FILES), streams);

                    // Add HLS master playlist, also used for lives
                    final String masterPlaylistUrl = streamingPlaylist.getString(PLAYLIST_URL);
                    if (!isNullOrEmpty(masterPlaylistUrl)) {
                        final int id = streamingPlaylist.getInt("id", -1);
                        final String idStr;
                        if (id == -1) {
                            idStr = null;
                        } else {
                            idStr = String.valueOf(id);
                        }

                        // TODO: support mirrors
                        streams.add(new UriManifestStream(idStr, null,
                                new HttpDeliverySource(new UriObject(masterPlaylistUrl,
                                        UriObject.EXPIRATION_TIMESTAMP_NO_EXPIRY, null), List.of(),
                                        Map.of(), HttpDeliverySource.HttpMethod.GET, null),
                                StreamingProtocol.HLS));
                    }
                });

        // Fetch captions endpoint and add captions
        fetchAndBuildSubtitlesStreams(streams);

        return streams;
    }

    @Override
    public StreamType getStreamType() {
        return json.getBoolean("isLive") ? StreamType.LIVE_STREAM : StreamType.VIDEO_STREAM;
    }

    @Nullable
    @Override
    public StreamInfoItemsCollector getRelatedItems() throws IOException, ExtractionException {
        final List<String> tags = getTags();
        final String apiUrl;
        if (tags.isEmpty()) {
            apiUrl = baseUrl + "/api/v1/accounts/" + JsonUtils.getString(json, ACCOUNT_NAME)
                    + "@" + JsonUtils.getString(json, ACCOUNT_HOST)
                    + "/videos?start=0&count=8";
        } else {
            apiUrl = getRelatedItemsUrl(tags);
        }

        if (Utils.isBlank(apiUrl)) {
            return null;
        } else {
            final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(
                    getServiceId());
            getStreamsFromApi(collector, apiUrl);
            return collector;
        }
    }

    @Nonnull
    @Override
    public List<String> getTags() {
        return JsonUtils.getStringListFromJsonArray(json.getArray("tags"));
    }

    @Nonnull
    @Override
    public String getSupportInfo() {
        try {
            return JsonUtils.getString(json, "support");
        } catch (final ParsingException e) {
            return "";
        }
    }

    @Nonnull
    @Override
    public List<StreamSegment> getStreamSegments() throws ParsingException {
        final List<StreamSegment> segments = new ArrayList<>();
        final JsonObject segmentsJson;
        try {
            segmentsJson = fetchSubApiContent("chapters");
        } catch (final IOException | ReCaptchaException e) {
            throw new ParsingException("Could not get stream segments", e);
        }
        if (segmentsJson != null && segmentsJson.has("chapters")) {
            final JsonArray segmentsArray = segmentsJson.getArray("chapters");
            for (int i = 0; i < segmentsArray.size(); i++) {
                final JsonObject segmentObject = segmentsArray.getObject(i);
                segments.add(new StreamSegment(
                        segmentObject.getString("title"),
                        segmentObject.getInt("timecode")));
            }
        }

        return segments;
    }

    @Nonnull
    @Override
    public List<Frameset> getFrames() throws ExtractionException {
        final List<Frameset> framesets = new ArrayList<>();
        final JsonObject storyboards;
        try {
            storyboards = fetchSubApiContent("storyboards");
        } catch (final IOException | ReCaptchaException e) {
            throw new ExtractionException("Could not get frames", e);
        }
        if (storyboards != null && storyboards.has("storyboards")) {
            final JsonArray storyboardsArray = storyboards.getArray("storyboards");
            for (final Object storyboard : storyboardsArray) {
                if (storyboard instanceof JsonObject) {
                    final JsonObject storyboardObject = (JsonObject) storyboard;
                    final String url = storyboardObject.getString("storyboardPath");
                    final int width = storyboardObject.getInt("spriteWidth");
                    final int height = storyboardObject.getInt("spriteHeight");
                    final int totalWidth = storyboardObject.getInt("totalWidth");
                    final int totalHeight = storyboardObject.getInt("totalHeight");
                    final int framesPerPageX = totalWidth / width;
                    final int framesPerPageY = totalHeight / height;
                    final int count = framesPerPageX * framesPerPageY;
                    final int durationPerFrame = storyboardObject.getInt("spriteDuration") * 1000;

                    framesets.add(new Frameset(
                            // there is only one composite image per video containing all frames
                            List.of(baseUrl + url),
                            width, height, count,
                            durationPerFrame, framesPerPageX, framesPerPageY));
                }
            }
        }

        return framesets;
    }

    @Nonnull
    private String getRelatedItemsUrl(@Nonnull final List<String> tags) {
        final String url = baseUrl + PeertubeSearchQueryHandlerFactory.SEARCH_ENDPOINT_VIDEOS;
        final StringBuilder params = new StringBuilder();
        params.append("start=0&count=8&sort=-createdAt");
        for (final String tag : tags) {
            params.append("&tagsOneOf=").append(Utils.encodeUrlUtf8(tag));
        }
        return url + "?" + params;
    }

    private void getStreamsFromApi(final StreamInfoItemsCollector collector, final String apiUrl)
            throws IOException, ReCaptchaException, ParsingException {
        final Response response = getDownloader().get(apiUrl);
        JsonObject relatedVideosJson = null;
        if (response != null && !Utils.isBlank(response.responseBody())) {
            try {
                relatedVideosJson = JsonParser.object().from(response.responseBody());
            } catch (final JsonParserException e) {
                throw new ParsingException("Could not parse json data for related videos", e);
            }
        }

        if (relatedVideosJson != null) {
            collectStreamsFrom(collector, relatedVideosJson);
        }
    }

    private void collectStreamsFrom(final StreamInfoItemsCollector collector,
                                    final JsonObject jsonObject) throws ParsingException {
        final JsonArray contents;
        try {
            contents = (JsonArray) JsonUtils.getValue(jsonObject, "data");
        } catch (final Exception e) {
            throw new ParsingException("Could not extract related videos", e);
        }

        for (final Object c : contents) {
            if (c instanceof JsonObject) {
                final JsonObject item = (JsonObject) c;
                final PeertubeStreamInfoItemExtractor extractor =
                        new PeertubeStreamInfoItemExtractor(item, baseUrl);
                // Do not add the same stream in related streams
                if (!extractor.getUrl().equals(getUrl())) {
                    collector.commit(extractor);
                }
            }
        }
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        final Response response = downloader.get(
                baseUrl + PeertubeStreamLinkHandlerFactory.VIDEO_API_ENDPOINT + getId());
        if (response != null) {
            setInitialData(response.responseBody());
        } else {
            throw new ExtractionException("Could not extract PeerTube channel data");
        }
    }

    private void setInitialData(final String responseBody) throws ExtractionException {
        try {
            json = JsonParser.object().from(responseBody);
        } catch (final JsonParserException e) {
            throw new ExtractionException("Could not extract PeerTube stream data", e);
        }
        if (json == null) {
            throw new ExtractionException("Could not extract PeerTube stream data");
        }
        PeertubeParsingHelper.validate(json);
    }

    private void fetchAndBuildSubtitlesStreams(@Nonnull final List<Stream> streams) {
        // Cache the results to avoid requesting the captions endpoint at each getStreams call
        if (cachedSubtitlesStreams == null) {
            final Response response;
            try {
                response = getDownloader().get(baseUrl
                        + PeertubeStreamLinkHandlerFactory.VIDEO_API_ENDPOINT
                        + getId() + "/captions");
            } catch (final IOException | ExtractionException ignored) {
                // Could not fetch captions JSON endpoint
                // As we get at the same time subtitles and other streams, subtitles are not
                // mandatory to play contents, and we don't have a logging system, we ignore the
                // exception
                return;
            }

            final JsonObject captionsJson;
            try {
                captionsJson = JsonParser.object()
                        .from(response.responseBody());
            } catch (final JsonParserException e) {
                // Could not parse captions JSON response
                // As we get at the same time subtitles and other streams, subtitles are not
                // mandatory to play contents, and we don't have a logging system, we ignore the
                // exception
                return;
            }

            final JsonArray captions = captionsJson.getArray("data");

            // Only initialize subtitles here once we are sure we can get subtitles
            cachedSubtitlesStreams = new ArrayList<>(captionsJson.getInt("total"));

            captions.streamAsJsonObjects()
                    .forEach(this::buildSubtitlesStreams);
        }

        if (cachedSubtitlesStreams != null) {
            streams.addAll(cachedSubtitlesStreams);
        }
    }

    private void buildSubtitlesStreams(@Nonnull final JsonObject subtitlesStreams) {
        String progressiveUrl = subtitlesStreams.getString(FILE_URL);
        if (isNullOrEmpty(progressiveUrl)) {
            // fileUrl has been introduced in PeerTube 7.1, so if we parse
            // an older instance we won't get the subtitles URL
            // Fall back to captionPath if not present, deprecated in 8.0
            final String captionPath = subtitlesStreams.getString("captionPath");
            if (isNullOrEmpty(captionPath)) {
                // As we cannot get the URL, we cannot guess the media format
                // from the file extension, ignore this stream
                return;
            }

            progressiveUrl = baseUrl + captionPath;
        }

        final boolean isAutomaticallyGenerated =
                subtitlesStreams.getBoolean("automaticallyGenerated");

        final String name = subtitlesStreams.getObject("language")
                .getString("label");

        final Locale locale = LocaleCompat.forLanguageTag(subtitlesStreams.getObject("language")
                        .getString("id"))
                .orElse(null);

        final SubtitlesMediaFormat subtitlesMediaFormat = SubtitlesMediaFormat.getFromSuffix(
                progressiveUrl.substring(progressiveUrl.lastIndexOf(".") + 1));
        if (subtitlesMediaFormat != null) {
            cachedSubtitlesStreams.add(buildSubtitlesStream(name, locale, isAutomaticallyGenerated,
                    subtitlesMediaFormat, progressiveUrl, StreamingProtocol.PROGRESSIVE));

            final String hlsUrl = subtitlesStreams.getString("m3u8Url");
            if (!isNullOrEmpty(hlsUrl)) {
                cachedSubtitlesStreams.add(buildSubtitlesStream(name, locale,
                        isAutomaticallyGenerated, subtitlesMediaFormat,
                        hlsUrl, StreamingProtocol.HLS));
            }
        }
    }

    @Nonnull
    private UriSubtitlesStream buildSubtitlesStream(
            @Nullable final String name,
            @Nullable final Locale locale,
            final boolean isAutomaticallyGenerated,
            @Nonnull final SubtitlesMediaFormat subtitlesMediaFormat,
            @Nonnull final String url,
            @Nonnull final StreamingProtocol streamingProtocol) {
        return new UriSubtitlesStream(null, name, locale, isAutomaticallyGenerated,
                subtitlesMediaFormat, List.of(), new HttpDeliverySource(new UriObject(url,
                UriObject.EXPIRATION_TIMESTAMP_NO_EXPIRY, null), List.of(), Map.of(),
                HttpDeliverySource.HttpMethod.GET, null), streamingProtocol);
    }

    private void getStreamsFromArray(@Nonnull final JsonArray streamsArray,
                                     @Nonnull final List<Stream> streams) {
        streamsArray.streamAsJsonObjects()
                .forEach(stream -> {
                    if (stream.getBoolean("hasAudio")) {
                        if (stream.getBoolean("hasVideo")) {
                            // Muxed video stream with audio
                            buildVideoStreams(stream, streams, true);
                        } else {
                            // Demuxed audio
                            buildAudioStreams(stream, streams);
                        }
                    } else if (stream.getBoolean("hasVideo")) {
                        // Demuxed video stream
                        buildVideoStreams(stream, streams, false);
                    }
                });
    }

    private void buildAudioStreams(@Nonnull final JsonObject audioStream,
                                   @Nonnull final List<Stream> streamsList) {
        final int id = audioStream.getInt("id", -1);
        final String idStr;
        if (id == -1) {
            idStr = null;
        } else {
            idStr = String.valueOf(id);
        }

        final String name = audioStream.getObject("resolution")
                .getString("label");
        final String progressiveUrl = audioStream.getString(FILE_URL,
                audioStream.getString(FILE_DOWNLOAD_URL));

        if (!isNullOrEmpty(progressiveUrl)) {
            streamsList.add(buildAudioStream(idStr, name, progressiveUrl,
                    StreamingProtocol.PROGRESSIVE));
        }

        // Only provided in streamingPlaylists
        final String playlistUrl = audioStream.getString(PLAYLIST_URL);
        if (!isNullOrEmpty(playlistUrl)) {
            streamsList.add(buildAudioStream(idStr, name, playlistUrl, StreamingProtocol.HLS));
        }
    }

    @Nonnull
    private UriAudioStream buildAudioStream(@Nullable final String id,
                                            @Nullable final String name,
                                            @Nonnull final String url,
                                            @Nonnull final StreamingProtocol streamingProtocol) {
        // PeerTube currently only uses MP4 containers, see
        // https://github.com/Chocobozzz/PeerTube/issues/4356
        // TODO: support mirrors
        return new UriAudioStream(id, name, null, Boolean.FALSE, AudioMediaFormat.M4A, null, null,
                null, AudioTrackType.ORIGINAL, Boolean.FALSE, null, new HttpDeliverySource(
                        new UriObject(url, UriObject.EXPIRATION_TIMESTAMP_NO_EXPIRY, null),
                        List.of(), Map.of(), HttpDeliverySource.HttpMethod.GET, null),
                streamingProtocol);
    }

    private void buildVideoStreams(@Nonnull final JsonObject videoStream,
                                   @Nonnull final List<Stream> streams,
                                   final boolean isMuxedStream) {
        final int id = videoStream.getInt("id", -1);
        final String idStr;
        if (id == -1) {
            idStr = null;
        } else {
            idStr = String.valueOf(id);
        }

        Integer width = videoStream.getInt("width", -1);
        if (width == -1) {
            width = null;
        }

        Integer height = videoStream.getInt("height", -1);
        if (height == -1) {
            height = null;
        }

        Integer fps = videoStream.getInt("fps", -1);
        if (fps == -1) {
            fps = null;
        }

        final String name = videoStream.getObject("resolution")
                .getString("label");
        final String progressiveUrl = videoStream.getString(FILE_URL,
                videoStream.getString(FILE_DOWNLOAD_URL));

        if (!isNullOrEmpty(progressiveUrl)) {
            if (isMuxedStream) {
                streams.add(buildMuxedStream(idStr, name, width, height, fps,
                        progressiveUrl, StreamingProtocol.PROGRESSIVE));
            } else {
                streams.add(buildVideoStream(idStr, name, width, height, fps,
                        progressiveUrl, StreamingProtocol.PROGRESSIVE));
            }
        }

        // Only provided in streamingPlaylists
        final String playlistUrl = videoStream.getString(PLAYLIST_URL);
        if (!isNullOrEmpty(playlistUrl)) {
            if (isMuxedStream) {
                streams.add(buildMuxedStream(idStr, name, width, height, fps, playlistUrl,
                        StreamingProtocol.HLS));
            } else {
                streams.add(buildVideoStream(idStr, name, width, height, fps, playlistUrl,
                        StreamingProtocol.HLS));
            }
        }
    }

    @Nonnull
    private UriVideoStream buildVideoStream(@Nullable final String id,
                                            @Nullable final String name,
                                            @Nullable final Integer width,
                                            @Nullable final Integer height,
                                            @Nullable final Integer framerate,
                                            @Nonnull final String url,
                                            @Nonnull final StreamingProtocol streamingProtocol) {
        // PeerTube currently only uses MP4 containers, see
        // https://github.com/Chocobozzz/PeerTube/issues/4356
        // TODO: support mirrors
        return new UriVideoStream(id, name, null, Boolean.FALSE, VideoMediaFormat.MP4,
                BaseVideoStream.ProjectionType.UNKNOWN, null, width, height, framerate, null, null,
                new HttpDeliverySource(new UriObject(url, UriObject.EXPIRATION_TIMESTAMP_NO_EXPIRY,
                        null), List.of(), Map.of(), HttpDeliverySource.HttpMethod.GET, null),
                streamingProtocol);
    }

    @Nonnull
    private UriMuxedStream buildMuxedStream(@Nullable final String id,
                                            @Nullable final String name,
                                            @Nullable final Integer width,
                                            @Nullable final Integer height,
                                            @Nullable final Integer framerate,
                                            @Nonnull final String url,
                                            @Nonnull final StreamingProtocol streamingProtocol) {
        // Muxed streams should contain only one audio track
        // See https://github.com/Chocobozzz/PeerTube/issues/939
        // PeerTube currently only uses MP4 containers, see
        // https://github.com/Chocobozzz/PeerTube/issues/4356
        // TODO: support mirrors
        return new UriMuxedStream(
                id,
                name,
                List.of(new BaseAudioStreamImpl(null, null, null, Boolean.FALSE,
                        AudioMediaFormat.M4A, null, null, null, AudioTrackType.ORIGINAL,
                        Boolean.FALSE, null)),
                List.of(new BaseVideoStreamImpl(null, null, null, Boolean.FALSE,
                        VideoMediaFormat.MP4, BaseVideoStream.ProjectionType.UNKNOWN, null, width,
                        height, framerate, null, null)),
                List.of(),
                List.of(),
                null,
                new HttpDeliverySource(new UriObject(url, UriObject.EXPIRATION_TIMESTAMP_NO_EXPIRY,
                        null), List.of(), Map.of(), HttpDeliverySource.HttpMethod.GET, null),
                streamingProtocol);
    }

    /**
     * Fetch content from a sub-API of the video.
     * @param subPath the API subpath after the video id,
     *                e.g. "storyboards" for "/api/v1/videos/{id}/storyboards"
     * @return the {@link JsonObject} of the sub-API or null if the API does not exist
     * which is the case if the instance has an outdated PeerTube version.
     * @throws ParsingException if the API response could not be parsed to a {@link JsonObject}
     * @throws IOException if the API response could not be fetched
     * @throws ReCaptchaException if the API response is a reCaptcha
     */
    @Nullable
    private JsonObject fetchSubApiContent(@Nonnull final String subPath)
            throws ParsingException, IOException, ReCaptchaException {
        final String apiUrl = baseUrl + PeertubeStreamLinkHandlerFactory.VIDEO_API_ENDPOINT
                + getId() + "/" + subPath;
        final Response response = getDownloader().get(apiUrl);
        if (response == null) {
            throw new ParsingException("Could not get segments from API.");
        }
        if (response.responseCode() == 400) {
            // Chapter or segments support was added with PeerTube v6.0.0
            // This instance does not support it yet.
            return null;
        }
        if (response.responseCode() != 200) {
            throw new ParsingException("Could not get segments from API. Response code: "
                    + response.responseCode());
        }
        try {
            return JsonParser.object().from(response.responseBody());
        } catch (final JsonParserException e) {
            throw new ParsingException("Could not parse json data for segments", e);
        }
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return JsonUtils.getString(json, "name");
    }

    @Nonnull
    @Override
    public String getHost() throws ParsingException {
        return JsonUtils.getString(json, ACCOUNT_HOST);
    }

    @Nonnull
    @Override
    public Privacy getPrivacy() {
        switch (json.getObject("privacy").getInt("id")) {
            case 1:
                return Privacy.PUBLIC;
            case 2:
                return Privacy.UNLISTED;
            case 3:
                return Privacy.PRIVATE;
            case 4:
                return Privacy.INTERNAL;
            default:
                return Privacy.OTHER;
        }
    }

    @Nonnull
    @Override
    public String getCategory() throws ParsingException {
        return JsonUtils.getString(json, "category.label");
    }

    @Nonnull
    @Override
    public String getLicence() throws ParsingException {
        return JsonUtils.getString(json, "licence.label");
    }

    @Override
    public Locale getLanguageInfo() {
        try {
            return new Locale(JsonUtils.getString(json, "language.id"));
        } catch (final ParsingException e) {
            return null;
        }
    }
}
