package org.schabi.newpipe.extractor.services.peertube.extractors;

import static org.schabi.newpipe.extractor.utils.Utils.UTF_8;
import static org.schabi.newpipe.extractor.utils.Utils.isNullOrEmpty;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

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
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.Privacy;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.streamdata.delivery.DeliveryData;
import org.schabi.newpipe.extractor.streamdata.delivery.simpleimpl.SimpleHLSDeliveryDataImpl;
import org.schabi.newpipe.extractor.streamdata.delivery.simpleimpl.SimpleProgressiveHTTPDeliveryDataImpl;
import org.schabi.newpipe.extractor.streamdata.delivery.simpleimpl.SimpleTorrentDeliveryDataImpl;
import org.schabi.newpipe.extractor.streamdata.format.registry.AudioFormatRegistry;
import org.schabi.newpipe.extractor.streamdata.format.registry.SubtitleFormatRegistry;
import org.schabi.newpipe.extractor.streamdata.format.registry.VideoAudioFormatRegistry;
import org.schabi.newpipe.extractor.streamdata.stream.AudioStream;
import org.schabi.newpipe.extractor.streamdata.stream.Stream;
import org.schabi.newpipe.extractor.streamdata.stream.SubtitleStream;
import org.schabi.newpipe.extractor.streamdata.stream.VideoAudioStream;
import org.schabi.newpipe.extractor.streamdata.stream.quality.VideoQualityData;
import org.schabi.newpipe.extractor.streamdata.stream.simpleimpl.SimpleAudioStreamImpl;
import org.schabi.newpipe.extractor.streamdata.stream.simpleimpl.SimpleSubtitleStreamImpl;
import org.schabi.newpipe.extractor.streamdata.stream.simpleimpl.SimpleVideoAudioStreamImpl;
import org.schabi.newpipe.extractor.utils.JsonUtils;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
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
    private static final String STREAMING_PLAYLISTS = "streamingPlaylists";

    private final String baseUrl;
    private JsonObject json;

    private List<SubtitleStream> subtitles = null;
    private List<AudioStream> audioStreams = null;
    private List<VideoAudioStream> videoStreams = null;

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
        return JsonUtils.getBoolean(json, "nsfw") ? 18 : NO_AGE_LIMIT;
    }

    @Override
    public long getLength() {
        return json.getLong("duration");
    }

    @Override
    public long getTimeStamp() throws ParsingException {
        final long timestamp = getTimestampSeconds(
                "((#|&|\\?)start=\\d{0,3}h?\\d{0,3}m?\\d{1,3}s?)");

        return (timestamp == -2)
                ? 0 // regex for timestamp was not found
                : timestamp;
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
    public String getHlsMasterPlaylistUrl() throws ParsingException {
        assertPageFetched();

        if (!isLive() && !isNullOrEmpty(json.getObject(FILES))) {
            return json.getObject(FILES).getString(PLAYLIST_URL, "");
        }

        return json.getArray(STREAMING_PLAYLISTS)
                .getObject(0)
                .getString(PLAYLIST_URL, "");
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
        tryExtractStreams();

        return audioStreams;
    }

    @Override
    public List<VideoAudioStream> getVideoStreams() throws ExtractionException {
        assertPageFetched();

        tryExtractStreams();

        return videoStreams;
    }

    @Nonnull
    @Override
    public List<SubtitleStream> getSubtitles() {
        assertPageFetched();
        return subtitles;
    }

    @Override
    public boolean isLive() {
        return json.getBoolean("isLive");
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
        final String url = baseUrl + PeertubeSearchQueryHandlerFactory.SEARCH_ENDPOINT;
        final StringBuilder params = new StringBuilder();
        params.append("start=0&count=8&sort=-createdAt");
        for (final String tag : tags) {
            params.append("&tagsOneOf=");
            params.append(URLEncoder.encode(tag, UTF_8));
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

        tryExtractSubtitles();
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

    private void tryExtractSubtitles() {
        if (subtitles != null) {
            return;
        }
        try {
            final Response response = getDownloader().get(baseUrl
                    + PeertubeStreamLinkHandlerFactory.VIDEO_API_ENDPOINT
                    + getId() + "/captions");
            final JsonObject captionsJson = JsonParser.object().from(response.responseBody());
            final JsonArray captions = JsonUtils.getArray(captionsJson, "data");

            subtitles = captions.stream()
                    .filter(JsonObject.class::isInstance)
                    .map(JsonObject.class::cast)
                    .map(caption -> {
                        try {
                            final String url =
                                    baseUrl + JsonUtils.getString(caption, "captionPath");

                            return new SimpleSubtitleStreamImpl(
                                    new SubtitleFormatRegistry()
                                            .getFromSuffixOrThrow(
                                                    url.substring(url.lastIndexOf(".") + 1)),
                                    new SimpleProgressiveHTTPDeliveryDataImpl(url),
                                    false,
                                    JsonUtils.getString(caption, "language.id")
                            );
                        } catch (final Exception ignored) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (final Exception ignored) {
            subtitles = Collections.emptyList();
        }
    }

    private void tryExtractStreams() throws ParsingException {
        if (audioStreams != null && videoStreams != null) {
            return;
        }

        // Initialize
        audioStreams = new ArrayList<>();
        videoStreams = new ArrayList<>();

        // Progressive streams
        try {
            addStreamsFromArray(
                    json.getArray(FILES),
                    null);
        } catch (final Exception e) {
            throw new ParsingException("Could not add HLS streams", e);
        }

        // HLS streams
        try {
            json.getArray(STREAMING_PLAYLISTS).stream()
                    .filter(JsonObject.class::isInstance)
                    .map(JsonObject.class::cast)
                    .forEach(playlist -> addStreamsFromArray(
                            playlist.getArray(FILES),
                            playlist.getString(PLAYLIST_URL)));
        } catch (final Exception e) {
            throw new ParsingException("Could not add HLS streams", e);
        }
    }

    private void addStreamsFromArray(
            @Nonnull final JsonArray streams,
            final String playlistUrl
    ) {
        streams.stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .filter(stream -> !isNullOrEmpty(getUrlFromStream(stream)))
                .forEach(stream -> {
                    final JsonObject resJson = stream.getObject("resolution");
                    if (resJson.getString("label", "")
                            .toLowerCase()
                            .contains("audio")) {
                        // An audio stream
                        addNewStreams(
                                this.audioStreams,
                                stream,
                                playlistUrl,
                                (s, dd) -> new SimpleAudioStreamImpl(
                                        new AudioFormatRegistry()
                                                .getFromSuffixOrThrow(getExtensionFromStream(s)),
                                        dd
                                )
                        );

                    } else {
                        // A video stream
                        addNewStreams(
                                this.videoStreams,
                                stream,
                                playlistUrl,
                                (s, dd) -> new SimpleVideoAudioStreamImpl(
                                        new VideoAudioFormatRegistry()
                                                .getFromSuffixOrThrow(getExtensionFromStream(s)),
                                        dd,
                                        VideoQualityData.fromHeightFps(
                                                resJson.getInt("id", VideoQualityData.UNKNOWN),
                                                stream.getInt("fps", VideoQualityData.UNKNOWN))
                                )
                        );
                    }
                });
    }


    private static String getStreamUrlKeyFromStream(@Nonnull final JsonObject stream) {
        return stream.has(FILE_URL) ? FILE_URL : FILE_DOWNLOAD_URL;
    }

    private static String getUrlFromStream(@Nonnull final JsonObject stream) {
        return stream.getString(getStreamUrlKeyFromStream(stream));
    }

    private static String getExtensionFromStream(@Nonnull final JsonObject stream) {
        final String url = stream.getString(getStreamUrlKeyFromStream(stream));
        return url.substring(url.lastIndexOf(".") + 1);
    }

    private <S extends Stream> void addNewStreams(
            final List<S> streams,
            @Nonnull final JsonObject stream,
            final String playlistUrl,
            @Nonnull final BiFunction<JsonObject, DeliveryData, S> buildStream
    ) {
        final Consumer<DeliveryData> addDeliveryDataToStream =
                dd -> {
                    try {
                        streams.add(buildStream.apply(stream, dd));
                    } catch (final Exception ignored) {
                        // Ignore exception when a single stream couldn't be added
                    }
                };

        // Add Progressive HTTP (this is also done for HLS streams because the source file can
        // also be streamed over progressive HTTP)
        addDeliveryDataToStream.accept(
                new SimpleProgressiveHTTPDeliveryDataImpl(getUrlFromStream(stream)));

        // Add HLS (only for PeerTube 3.4+)
        if (!isNullOrEmpty(playlistUrl)
                && playlistUrl.endsWith("-master.m3u8")
                && !isNullOrEmpty(stream.getString(FILE_URL))
        ) {
            addDeliveryDataToStream.accept(new SimpleHLSDeliveryDataImpl(stream.getString(FILE_URL)
                    .replace("-fragmented." + getExtensionFromStream(stream), ".m3u8")));
        }

        // Add torrent
        if (!isNullOrEmpty(stream.getString("torrentUrl"))) {
            addDeliveryDataToStream.accept(
                    new SimpleTorrentDeliveryDataImpl(stream.getString("torrentUrl")));
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
