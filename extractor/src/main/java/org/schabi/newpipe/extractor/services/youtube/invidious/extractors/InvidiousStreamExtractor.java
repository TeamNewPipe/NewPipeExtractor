package org.schabi.newpipe.extractor.services.youtube.invidious.extractors;

import static org.schabi.newpipe.extractor.utils.Utils.isBlank;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousParsingHelper;
import org.schabi.newpipe.extractor.services.youtube.invidious.InvidiousService;
import org.schabi.newpipe.extractor.services.youtube.shared.ItagItem;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.Frameset;
import org.schabi.newpipe.extractor.stream.Stream;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamInfoItemsCollector;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.SubtitlesStream;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.schabi.newpipe.extractor.utils.JsonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InvidiousStreamExtractor extends StreamExtractor {

    private final String baseUrl;
    private JsonObject json;

    public InvidiousStreamExtractor(final InvidiousService service, final LinkHandler linkHandler) {
        super(service, linkHandler);
        baseUrl = service.getInstance().getUrl();
    }

    @Override
    public void onFetchPage(
            @Nonnull final Downloader downloader
    ) throws IOException, ExtractionException {
        final String apiUrl = baseUrl + "/api/v1/videos/" + getId()
                + "?region=" + getExtractorContentCountry().getCountryCode();

        final Response response = downloader.get(apiUrl);

        json = InvidiousParsingHelper.getValidJsonObjectFromResponse(response, apiUrl);
    }

    @Nullable
    @Override
    public String getTextualUploadDate() {
        // Note: Depends on instance localization
        return json.getString("publishedText");
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() {
        return InvidiousParsingHelper.getUploadDateFromEpochTime(
                json.getNumber("published").longValue());
    }

    @Nonnull
    @Override
    public String getThumbnailUrl() {
        final JsonArray thumbnail = json.getArray("authorThumbnails");
        return InvidiousParsingHelper.getThumbnailUrl(thumbnail);
    }

    @Nonnull
    @Override
    public Description getDescription() {
        final String descriptionHtml = json.getString("descriptionHtml");
        if (!isBlank(descriptionHtml) || "<p></p>".equals(descriptionHtml)) {
            return new Description(descriptionHtml, Description.HTML);
        }

        return new Description(json.getString("description"), Description.PLAIN_TEXT);
    }

    @Override
    public int getAgeLimit() {
        final boolean isFamilyFriendly = json.getBoolean("isFamilyFriendly");
        return isFamilyFriendly ? NO_AGE_LIMIT : 18;
    }

    @Override
    public long getLength() {
        return json.getNumber("lengthSeconds").longValue();
    }

    @Override
    public long getTimeStamp() throws ParsingException {
        return getTimestampSeconds("((#|&|\\?)t=\\d{0,3}h?\\d{0,3}m?\\d{1,3}s?)");
        // from YouTubeStreamExtractor
    }

    @Override
    public long getViewCount() {
        return json.getNumber("viewCount").longValue();
    }

    @Override
    public long getLikeCount() {
        return json.getNumber("likeCount").longValue();
    }

    @Override
    public long getDislikeCount() {
        return json.getNumber("dislikeCount").longValue();
    }

    @Nonnull
    @Override
    public String getUploaderUrl() {
        return baseUrl + json.getString("authorUrl");
    }

    @Nonnull
    @Override
    public String getUploaderName() {
        return json.getString("author");
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return false;
    }

    @Nonnull
    @Override
    public String getUploaderAvatarUrl() {
        final JsonArray avatars = json.getArray("authorThumbnails");
        return InvidiousParsingHelper.getThumbnailUrl(avatars);
    }

    @Nonnull
    @Override
    public String getDashMpdUrl() {
        return baseUrl + json.getString("dashUrl");
    }

    @Nonnull
    @Override
    public String getHlsUrl() {
        final String hlsUrl = json.getString("hlsUrl");
        return hlsUrl != null ? hlsUrl : "";
    }

    @Override
    public List<AudioStream> getAudioStreams() throws ExtractionException {
        return getStreams(
                getItags("adaptiveFormats", ItagItem.ItagType.AUDIO),
                AudioStream::new
        );
    }

    @Override
    public List<VideoStream> getVideoStreams() throws ExtractionException {
        return getStreams(
                getItags("formatStreams", ItagItem.ItagType.VIDEO),
                (url, itag) -> new VideoStream(url, false, itag)
        );
    }

    @Override
    public List<VideoStream> getVideoOnlyStreams() throws ExtractionException {
        return getStreams(
                getItags("adaptiveFormats", ItagItem.ItagType.VIDEO_ONLY),
                (url, itag) -> new VideoStream(url, true, itag)
        );
    }

    private <T extends Stream> List<T> getStreams(
            final Map<String, ItagItem> itags,
            final BiFunction<String, ItagItem, T> newStreamCreator
    ) throws ExtractionException {
        final List<T> streams = new ArrayList<>();
        try {
            for (final Map.Entry<String, ItagItem> entry : itags.entrySet()) {
                final T videoStream = newStreamCreator.apply(entry.getKey(), entry.getValue());
                if (!Stream.containSimilarStream(videoStream, streams)) {
                    streams.add(videoStream);
                }
            }
        } catch (final Exception e) {
            throw new ParsingException("Could not get video only streams", e);
        }

        return streams;
    }

    private Map<String, ItagItem> getItags(
            final String streamingDataKey,
            final ItagItem.ItagType itagTypeWanted
    ) throws ParsingException {
        final JsonArray formats = json.getArray(streamingDataKey);

        final Map<String, ItagItem> urlAndItags = new LinkedHashMap<>();
        for (final Object o : formats) {
            final JsonObject formatData = (JsonObject) o;
            final int itag = Integer.parseInt(formatData.getString("itag"));

            if (ItagItem.isSupported(itag)) {
                final ItagItem itagItem = ItagItem.getItag(itag);
                if (itagItem.itagType == itagTypeWanted) {
                    urlAndItags.put(formatData.getString("url"), itagItem);
                }
            }
        }

        return urlAndItags;
    }

    @Nonnull
    @Override
    public List<SubtitlesStream> getSubtitlesDefault() {
        return getSubtitles(MediaFormat.VTT);
    }

    @Nonnull
    @Override
    public List<SubtitlesStream> getSubtitles(final MediaFormat format) {
        return json.getArray("captions").stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .filter(obj -> obj.has("language_code")) // Prevent NPE below
                .map(obj -> {
                    final String languageCode = obj.getString("language_code");
                    return new SubtitlesStream(
                            format,
                            languageCode,
                            baseUrl + obj.getString("url"),
                            languageCode.contains("(auto-generated)"));
                })
                .collect(Collectors.toList());
    }

    @Override
    public StreamType getStreamType() {
        if (json.getBoolean("liveNow")) {
            return StreamType.LIVE_STREAM;
        }
        return StreamType.VIDEO_STREAM;
    }

    @Nullable
    @Override
    public StreamInfoItemsCollector getRelatedItems() throws IOException, ExtractionException {
        try {
            final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
            json.getArray("recommendedVideos").stream()
                    .filter(JsonObject.class::isInstance)
                    .map(JsonObject.class::cast)
                    .forEach(o -> new InvidiousStreamInfoItemExtractor(o, baseUrl));

            return collector;
        } catch (final Exception e) {
            throw new ParsingException("Could not get related videos", e);
        }
    }

    @Nonnull
    @Override
    public Privacy getPrivacy() {
        return json.getBoolean("isListed") ? Privacy.PUBLIC : Privacy.UNLISTED;
    }

    @Nonnull
    @Override
    public String getCategory() {
        return json.getString("genre");
    }

    @Nonnull
    @Override
    public List<String> getTags() {
        return JsonUtils.getStringListFromJsonArray(json.getArray("keywords"));
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return json.getString("title");
    }

    @Nonnull
    @Override
    public List<Frameset> getFrames() throws ExtractionException {
        return json.getArray("storyboards").stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .map(storyboard -> {
                    final int durationPerFrames = storyboard.getInt("interval");
                    if (durationPerFrames == 0) {
                        return null;
                    }

                    final String templateUrl = storyboard.getString("templateUrl");
                    final List<String> urls =
                            IntStream.range(0, storyboard.getInt("storyboardCount"))
                                    .mapToObj(i -> templateUrl.replace("M$M", "M" + i))
                                    .collect(Collectors.toList());

                    return new Frameset(
                            urls,
                            storyboard.getInt("width"),
                            storyboard.getInt("height"),
                            storyboard.getInt("count"),
                            durationPerFrames,
                            storyboard.getInt("storyboardWidth"),
                            storyboard.getInt("storyboardHeight")
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
