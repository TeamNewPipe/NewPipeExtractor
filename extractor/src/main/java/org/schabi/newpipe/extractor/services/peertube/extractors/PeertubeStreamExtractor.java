package org.schabi.newpipe.extractor.services.peertube.extractors;

import static org.schabi.newpipe.extractor.stream.AudioStream.UNKNOWN_BITRATE;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.schabi.newpipe.extractor.MediaFormat;
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
import org.schabi.newpipe.extractor.stream.DeliveryMethod;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.Stream;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.SubtitlesStream;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PeertubeStreamExtractor extends StreamExtractor {
    private static final String ACCOUNT_HOST = "account.host";
    private static final String ACCOUNT_NAME = "account.name";
    private static final String FILES = "files";
    private static final String FILE_DOWNLOAD_URL = "fileDownloadUrl";
    private static final String FILE_URL = "fileUrl";
    private static final String PLAYLIST_URL = "playlistUrl";
    private static final String RESOLUTION_ID = "resolution.id";
    private static final String STREAMING_PLAYLISTS = "streamingPlaylists";

    private final String baseUrl;
    private JsonObject json;

    private final List<SubtitlesStream> subtitles = new ArrayList<>();
    private final List<AudioStream> audioStreams = new ArrayList<>();
    private final List<VideoStream> videoStreams = new ArrayList<>();

    private ParsingException subtitlesException = null;

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
        final String textualUploadDate = getTextualUploadDate();

        if (textualUploadDate == null) {
            return null;
        }

        return new DateWrapper(PeertubeParsingHelper.parseDateFrom(textualUploadDate));
    }

    @Nonnull
    @Override
    public String getThumbnailUrl() throws ParsingException {
        return baseUrl + JsonUtils.getString(json, "previewPath");
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
        return new Description(text, Description.MARKDOWN);
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
    public String getUploaderAvatarUrl() {
        String value;
        try {
            value = JsonUtils.getString(json, "account.avatar.path");
        } catch (final Exception e) {
            value = "/client/assets/images/default-avatar.png";
        }
        return baseUrl + value;
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
    public String getSubChannelAvatarUrl() {
        String value;
        try {
            value = JsonUtils.getString(json, "channel.avatar.path");
        } catch (final Exception e) {
            value = "/client/assets/images/default-avatar.png";
        }
        return baseUrl + value;
    }

    @Nonnull
    @Override
    public String getHlsUrl() {
        assertPageFetched();

        if (getStreamType() == StreamType.VIDEO_STREAM
                && !isNullOrEmpty(json.getObject(FILES))) {
            return json.getObject(FILES).getString(PLAYLIST_URL, "");
        }

        return json.getArray(STREAMING_PLAYLISTS).getObject(0).getString(PLAYLIST_URL, "");
    }

    @Override
    public List<AudioStream> getAudioStreams() throws ParsingException {
        assertPageFetched();

        /*
        Some videos have audio streams; others don't.
        So an audio stream may be available if a video stream is available.
        Audio streams are also not returned as separated streams for livestreams.
        That's why the extraction of audio streams is only run when there are video streams
        extracted and when the content is not a livestream.
         */
        if (audioStreams.isEmpty() && videoStreams.isEmpty()
                && getStreamType() == StreamType.VIDEO_STREAM) {
            getStreams();
        }

        return audioStreams;
    }

    @Override
    public List<VideoStream> getVideoStreams() throws ExtractionException {
        assertPageFetched();

        if (videoStreams.isEmpty()) {
            if (getStreamType() == StreamType.VIDEO_STREAM) {
                getStreams();
            } else {
                extractLiveVideoStreams();
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
    public List<SubtitlesStream> getSubtitlesDefault() throws ParsingException {
        if (subtitlesException != null) {
            throw subtitlesException;
        }
        return subtitles;
    }

    @Nonnull
    @Override
    public List<SubtitlesStream> getSubtitles(final MediaFormat format) throws ParsingException {
        if (subtitlesException != null) {
            throw subtitlesException;
        }
        return subtitles.stream()
                .filter(sub -> sub.getFormat() == format)
                .collect(Collectors.toList());
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
    private String getRelatedItemsUrl(@Nonnull final List<String> tags)
            throws UnsupportedEncodingException {
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

        loadSubtitles();
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

    private void loadSubtitles() {
        if (subtitles.isEmpty()) {
            try {
                final Response response = getDownloader().get(baseUrl
                        + PeertubeStreamLinkHandlerFactory.VIDEO_API_ENDPOINT
                        + getId() + "/captions");
                final JsonObject captionsJson = JsonParser.object().from(response.responseBody());
                final JsonArray captions = JsonUtils.getArray(captionsJson, "data");
                for (final Object c : captions) {
                    if (c instanceof JsonObject) {
                        final JsonObject caption = (JsonObject) c;
                        final String url = baseUrl + JsonUtils.getString(caption, "captionPath");
                        final String languageCode = JsonUtils.getString(caption, "language.id");
                        final String ext = url.substring(url.lastIndexOf(".") + 1);
                        final MediaFormat fmt = MediaFormat.getFromSuffix(ext);
                        if (fmt != null && !isNullOrEmpty(languageCode)) {
                            subtitles.add(new SubtitlesStream.Builder()
                                    .setContent(url, true)
                                    .setMediaFormat(fmt)
                                    .setLanguageCode(languageCode)
                                    .setAutoGenerated(false)
                                    .build());
                        }
                    }
                }
            } catch (final Exception e) {
                subtitlesException = new ParsingException("Could not get subtitles", e);
            }
        }
    }

    private void extractLiveVideoStreams() throws ParsingException {
        try {
            final JsonArray streamingPlaylists = json.getArray(STREAMING_PLAYLISTS);
            streamingPlaylists.stream()
                    .filter(JsonObject.class::isInstance)
                    .map(JsonObject.class::cast)
                    .map(stream -> new VideoStream.Builder()
                            .setId(String.valueOf(stream.getInt("id", -1)))
                            .setContent(stream.getString(PLAYLIST_URL, ""), true)
                            .setIsVideoOnly(false)
                            .setResolution("")
                            .setMediaFormat(MediaFormat.MPEG_4)
                            .setDeliveryMethod(DeliveryMethod.HLS)
                            .build())
                    // Don't use the containsSimilarStream method because it will always return
                    // false so if there are multiples HLS URLs returned, only the first will be
                    // extracted in this case.
                    .forEachOrdered(videoStreams::add);
        } catch (final Exception e) {
            throw new ParsingException("Could not get video streams", e);
        }
    }

    private void getStreams() throws ParsingException {
        // Progressive streams
        getStreamsFromArray(json.getArray(FILES), "");

        // HLS streams
        try {
            for (final JsonObject playlist : json.getArray(STREAMING_PLAYLISTS).stream()
                    .filter(JsonObject.class::isInstance)
                    .map(JsonObject.class::cast)
                    .collect(Collectors.toList())) {
                getStreamsFromArray(playlist.getArray(FILES), playlist.getString(PLAYLIST_URL));
            }
        } catch (final Exception e) {
            throw new ParsingException("Could not get streams", e);
        }
    }

    private void getStreamsFromArray(@Nonnull final JsonArray streams,
                                     final String playlistUrl) throws ParsingException {
        try {
            /*
            Starting with version 3.4.0 of PeerTube, the HLS playlist of stream resolutions
            contains the UUID of the streams, so we can't use the same method to get the URL of
            the HLS playlist without fetching the master playlist.
            These UUIDs are the same as the ones returned into the fileUrl and fileDownloadUrl
            strings.
            */
            final boolean isInstanceUsingRandomUuidsForHlsStreams = !isNullOrEmpty(playlistUrl)
                    && playlistUrl.endsWith("-master.m3u8");

            for (final JsonObject stream : streams.stream()
                    .filter(JsonObject.class::isInstance)
                    .map(JsonObject.class::cast)
                    .collect(Collectors.toList())) {

                // Extract stream version of streams first
                final String url = JsonUtils.getString(stream,
                        stream.has(FILE_URL) ? FILE_URL : FILE_DOWNLOAD_URL);
                if (isNullOrEmpty(url)) {
                    // Not a valid stream URL
                    return;
                }

                final String resolution = JsonUtils.getString(stream, "resolution.label");
                final String idSuffix = stream.has(FILE_URL) ? FILE_URL : FILE_DOWNLOAD_URL;

                if (resolution.toLowerCase().contains("audio")) {
                    // An audio stream
                    addNewAudioStream(stream, isInstanceUsingRandomUuidsForHlsStreams, resolution,
                            idSuffix, url, playlistUrl);
                } else {
                    // A video stream
                    addNewVideoStream(stream, isInstanceUsingRandomUuidsForHlsStreams, resolution,
                            idSuffix, url, playlistUrl);
                }
            }
        } catch (final Exception e) {
            throw new ParsingException("Could not get streams from array", e);
        }
    }

    @Nonnull
    private String getHlsPlaylistUrlFromFragmentedFileUrl(
            @Nonnull final JsonObject streamJsonObject,
            @Nonnull final String idSuffix,
            @Nonnull final String format,
            @Nonnull final String url) throws ParsingException {
        final String streamUrl = FILE_DOWNLOAD_URL.equals(idSuffix)
                ? JsonUtils.getString(streamJsonObject, FILE_URL)
                : url;
        return streamUrl.replace("-fragmented." + format, ".m3u8");
    }

    @Nonnull
    private String getHlsPlaylistUrlFromMasterPlaylist(@Nonnull final JsonObject streamJsonObject,
                                                       @Nonnull final String playlistUrl)
            throws ParsingException {
        return playlistUrl.replace("master", JsonUtils.getNumber(streamJsonObject,
                RESOLUTION_ID).toString());
    }

    private void addNewAudioStream(@Nonnull final JsonObject streamJsonObject,
                                   final boolean isInstanceUsingRandomUuidsForHlsStreams,
                                   @Nonnull final String resolution,
                                   @Nonnull final String idSuffix,
                                   @Nonnull final String url,
                                   @Nullable final String playlistUrl) throws ParsingException {
        final String extension = url.substring(url.lastIndexOf(".") + 1);
        final MediaFormat format = MediaFormat.getFromSuffix(extension);
        final String id = resolution + "-" + extension;

        // Add progressive HTTP streams first
        audioStreams.add(new AudioStream.Builder()
                .setId(id + "-" + idSuffix + "-" + DeliveryMethod.PROGRESSIVE_HTTP)
                .setContent(url, true)
                .setMediaFormat(format)
                .setAverageBitrate(UNKNOWN_BITRATE)
                .build());

        // Then add HLS streams
        if (!isNullOrEmpty(playlistUrl)) {
            final String hlsStreamUrl;
            if (isInstanceUsingRandomUuidsForHlsStreams) {
                hlsStreamUrl = getHlsPlaylistUrlFromFragmentedFileUrl(streamJsonObject, idSuffix,
                        extension, url);

            } else {
                hlsStreamUrl = getHlsPlaylistUrlFromMasterPlaylist(streamJsonObject, playlistUrl);
            }
            final AudioStream audioStream = new AudioStream.Builder()
                    .setId(id + "-" + DeliveryMethod.HLS)
                    .setContent(hlsStreamUrl, true)
                    .setDeliveryMethod(DeliveryMethod.HLS)
                    .setMediaFormat(format)
                    .setAverageBitrate(UNKNOWN_BITRATE)
                    .setManifestUrl(playlistUrl)
                    .build();
            if (!Stream.containSimilarStream(audioStream, audioStreams)) {
                audioStreams.add(audioStream);
            }
        }

        // Finally, add torrent URLs
        final String torrentUrl = JsonUtils.getString(streamJsonObject, "torrentUrl");
        if (!isNullOrEmpty(torrentUrl)) {
            audioStreams.add(new AudioStream.Builder()
                    .setId(id + "-" + idSuffix + "-" + DeliveryMethod.TORRENT)
                    .setContent(torrentUrl, true)
                    .setDeliveryMethod(DeliveryMethod.TORRENT)
                    .setMediaFormat(format)
                    .setAverageBitrate(UNKNOWN_BITRATE)
                    .build());
        }
    }

    private void addNewVideoStream(@Nonnull final JsonObject streamJsonObject,
                                   final boolean isInstanceUsingRandomUuidsForHlsStreams,
                                   @Nonnull final String resolution,
                                   @Nonnull final String idSuffix,
                                   @Nonnull final String url,
                                   @Nullable final String playlistUrl) throws ParsingException {
        final String extension = url.substring(url.lastIndexOf(".") + 1);
        final MediaFormat format = MediaFormat.getFromSuffix(extension);
        final String id = resolution + "-" + extension;

        // Add progressive HTTP streams first
        videoStreams.add(new VideoStream.Builder()
                .setId(id + "-" + idSuffix + "-" + DeliveryMethod.PROGRESSIVE_HTTP)
                .setContent(url, true)
                .setIsVideoOnly(false)
                .setResolution(resolution)
                .setMediaFormat(format)
                .build());

        // Then add HLS streams
        if (!isNullOrEmpty(playlistUrl)) {
            final String hlsStreamUrl = isInstanceUsingRandomUuidsForHlsStreams
                    ? getHlsPlaylistUrlFromFragmentedFileUrl(streamJsonObject, idSuffix, extension,
                            url)
                    : getHlsPlaylistUrlFromMasterPlaylist(streamJsonObject, playlistUrl);

            final VideoStream videoStream = new VideoStream.Builder()
                    .setId(id + "-" + DeliveryMethod.HLS)
                    .setContent(hlsStreamUrl, true)
                    .setIsVideoOnly(false)
                    .setDeliveryMethod(DeliveryMethod.HLS)
                    .setResolution(resolution)
                    .setMediaFormat(format)
                    .setManifestUrl(playlistUrl)
                    .build();
            if (!Stream.containSimilarStream(videoStream, videoStreams)) {
                videoStreams.add(videoStream);
            }
        }

        // Add finally torrent URLs
        final String torrentUrl = JsonUtils.getString(streamJsonObject, "torrentUrl");
        if (!isNullOrEmpty(torrentUrl)) {
            videoStreams.add(new VideoStream.Builder()
                    .setId(id + "-" + idSuffix + "-" + DeliveryMethod.TORRENT)
                    .setContent(torrentUrl, true)
                    .setIsVideoOnly(false)
                    .setDeliveryMethod(DeliveryMethod.TORRENT)
                    .setResolution(resolution)
                    .setMediaFormat(format)
                    .build());
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
