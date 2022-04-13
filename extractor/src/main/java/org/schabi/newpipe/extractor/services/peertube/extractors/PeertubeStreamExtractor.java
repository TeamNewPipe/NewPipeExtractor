package org.schabi.newpipe.extractor.services.peertube.extractors;

import static org.schabi.newpipe.extractor.utils.Utils.UTF_8;
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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PeertubeStreamExtractor extends StreamExtractor {
    private final String baseUrl;
    private JsonObject json;
    private final List<SubtitlesStream> subtitles = new ArrayList<>();

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
            //if description is shortened, get full description
            final Downloader dl = NewPipe.getDownloader();
            try {
                final Response response = dl.get(baseUrl
                        + PeertubeStreamLinkHandlerFactory.VIDEO_API_ENDPOINT
                        + getId() + "/description");
                final JsonObject jsonObject = JsonParser.object().from(response.responseBody());
                text = JsonUtils.getString(jsonObject, "description");
            } catch (ReCaptchaException | IOException | JsonParserException e) {
                e.printStackTrace();
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
        final long timestamp =
                getTimestampSeconds("((#|&|\\?)start=\\d{0,3}h?\\d{0,3}m?\\d{1,3}s?)");

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
        final String name = JsonUtils.getString(json, "account.name");
        final String host = JsonUtils.getString(json, "account.host");
        return getService().getChannelLHFactory()
                .fromId("accounts/" + name + "@" + host, baseUrl).getUrl();
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
        return json.getArray("streamingPlaylists").getObject(0).getString("playlistUrl");
    }

    @Override
    public List<AudioStream> getAudioStreams() {
        return Collections.emptyList();
    }

    @Override
    public List<VideoStream> getVideoStreams() throws ExtractionException {
        assertPageFetched();
        final List<VideoStream> videoStreams = new ArrayList<>();

        // mp4
        try {
            videoStreams.addAll(getVideoStreamsFromArray(json.getArray("files")));
        } catch (final Exception ignored) { }

        // HLS
        try {
            final JsonArray streamingPlaylists = json.getArray("streamingPlaylists");
            for (final Object p : streamingPlaylists) {
                if (!(p instanceof JsonObject)) {
                    continue;
                }
                final JsonObject playlist = (JsonObject) p;
                videoStreams.addAll(getVideoStreamsFromArray(playlist.getArray("files")));
            }
        } catch (final Exception e) {
            throw new ParsingException("Could not get video streams", e);
        }

        if (getStreamType() == StreamType.LIVE_STREAM) {
            videoStreams.add(new VideoStream(getHlsUrl(), MediaFormat.MPEG_4, "720p"));
        }

        return videoStreams;
    }

    private List<VideoStream> getVideoStreamsFromArray(final JsonArray streams)
            throws ParsingException {
        try {
            final List<VideoStream> videoStreams = new ArrayList<>();
            for (final Object s : streams) {
                if (!(s instanceof JsonObject)) {
                    continue;
                }
                final JsonObject stream = (JsonObject) s;
                final String url;
                if (stream.has("fileDownloadUrl")) {
                    url = JsonUtils.getString(stream, "fileDownloadUrl");
                } else {
                    url = JsonUtils.getString(stream, "fileUrl");
                }
                final String torrentUrl = JsonUtils.getString(stream, "torrentUrl");
                final String resolution = JsonUtils.getString(stream, "resolution.label");
                final String extension = url.substring(url.lastIndexOf(".") + 1);
                final MediaFormat format = MediaFormat.getFromSuffix(extension);
                final VideoStream videoStream
                        = new VideoStream(url, torrentUrl, format, resolution);
                if (!Stream.containSimilarStream(videoStream, videoStreams)) {
                    videoStreams.add(videoStream);
                }
            }
            return videoStreams;
        } catch (final Exception e) {
            throw new ParsingException("Could not get video streams from array");
        }

    }

    @Override
    public List<VideoStream> getVideoOnlyStreams() {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public List<SubtitlesStream> getSubtitlesDefault() {
        return subtitles;
    }

    @Nonnull
    @Override
    public List<SubtitlesStream> getSubtitles(final MediaFormat format) {
        final List<SubtitlesStream> filteredSubs = new ArrayList<>();
        for (final SubtitlesStream sub : subtitles) {
            if (sub.getFormat() == format) {
                filteredSubs.add(sub);
            }
        }
        return filteredSubs;
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
            apiUrl = baseUrl + "/api/v1/accounts/" + JsonUtils.getString(json, "account.name")
                    + "@" + JsonUtils.getString(json, "account.host")
                    + "/videos?start=0&count=8";
        } else {
            apiUrl = getRelatedItemsUrl(tags);
        }

        if (Utils.isBlank(apiUrl)) {
            return null;
        } else {
            final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
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

    private String getRelatedItemsUrl(final List<String> tags) throws UnsupportedEncodingException {
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
            throws ReCaptchaException, IOException, ParsingException {
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
                                    final JsonObject jsonObject)
            throws ParsingException {
        final JsonArray contents;
        try {
            contents = (JsonArray) JsonUtils.getValue(jsonObject, "data");
        } catch (final Exception e) {
            throw new ParsingException("unable to extract related videos", e);
        }

        for (final Object c : contents) {
            if (c instanceof JsonObject) {
                final JsonObject item = (JsonObject) c;
                final PeertubeStreamInfoItemExtractor extractor
                        = new PeertubeStreamInfoItemExtractor(item, baseUrl);
                //do not add the same stream in related streams
                if (!extractor.getUrl().equals(getUrl())) {
                    collector.commit(extractor);
                }
            }
        }
    }

    @Override
    public void onFetchPage(final Downloader downloader) throws IOException, ExtractionException {
        final Response response = downloader.get(
                baseUrl + PeertubeStreamLinkHandlerFactory.VIDEO_API_ENDPOINT + getId());
        if (response != null) {
            setInitialData(response.responseBody());
        } else {
            throw new ExtractionException("Unable to extract PeerTube channel data");
        }

        loadSubtitles();
    }

    private void setInitialData(final String responseBody) throws ExtractionException {
        try {
            json = JsonParser.object().from(responseBody);
        } catch (final JsonParserException e) {
            throw new ExtractionException("Unable to extract PeerTube stream data", e);
        }
        if (json == null) {
            throw new ExtractionException("Unable to extract PeerTube stream data");
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
                            subtitles.add(new SubtitlesStream(fmt, languageCode, url, false));
                        }
                    }
                }
            } catch (final Exception e) {
                // ignore all exceptions
            }
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
        return JsonUtils.getString(json, "account.host");
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
