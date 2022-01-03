package org.schabi.newpipe.extractor.services.niconico.extractors;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.InfoItemExtractor;
import org.schabi.newpipe.extractor.InfoItemsCollector;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.VideoStream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NiconicoStreamExtractor extends StreamExtractor {
    private static final String THUMB_INFO_URL = "https://ext.nicovideo.jp/api/getthumbinfo/";
    private static final String UPLOADER_URL = "https://www.nicovideo.jp/user/";
    // generally, Niconico uses Japanese, but some videos have multiple language texts.
    // Use ja-JP locale to get original information of video.
    private final Localization LOCALE = Localization.fromLocalizationCode("ja-JP");
    private JsonObject watch;

    public NiconicoStreamExtractor(StreamingService service, LinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public long getViewCount() throws ParsingException {
        return watch.getObject("video").getObject("count").getLong("view");
    }

    @Override
    public long getLength() throws ParsingException {
        return watch.getObject("video").getLong("duration");
    }

    @Override
    public long getLikeCount() throws ParsingException {
        return  watch.getObject("video").getObject("count").getLong("like");
    }

    @Nonnull
    @Override
    public Description getDescription() throws ParsingException {
        return new Description(watch.getObject("video").getString("description"), 1);
    }

    @Nonnull
    @Override
    public String getThumbnailUrl() throws ParsingException {
        return watch.getObject("video").getObject("thumbnail").getString("url");
    }

    @Nonnull
    @Override
    public String getUploaderUrl() throws ParsingException {
        return UPLOADER_URL + watch.getObject("owner").getLong("id");
    }

    @Nonnull
    @Override
    public String getUploaderName() throws ParsingException {
        return watch.getObject("owner").getString("nickname");
    }

    @Nonnull
    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        return watch.getObject("owner").getString("iconUrl");
    }

    @Override
    public List<AudioStream> getAudioStreams() throws IOException, ExtractionException {
        return Collections.emptyList();
    }

    @Override
    public List<VideoStream> getVideoStreams() throws IOException, ExtractionException {
        final List<VideoStream> videoStreams = new ArrayList<>();

        final JsonObject session = watch.getObject("media").getObject("delivery").getObject("movie");
        final String dmc = session.getObject("session").getArray("urls").getObject(0).getString("url") + "?_format=json";
        final String s = NiconicoDMCPayloadBuilder.BuildJSON(session.getObject("session"));

        final Map<String, List<String>> headers = new HashMap<>();
        headers.put("Content-Type", Collections.singletonList("application/json"));

        final Response response = getDownloader().post(dmc, null, s.getBytes(StandardCharsets.UTF_8), LOCALE);

        try {
            final JsonObject content = JsonParser.object().from(response.responseBody());

            final String contentURL = content.getObject("data").getObject("session").getString("content_uri");
            videoStreams.add(new VideoStream(contentURL, MediaFormat.MPEG_4, "360p"));

        } catch (JsonParserException e) {
            throw new ExtractionException("could not get video contents.");
        }

        return  videoStreams;
    }

    @Override
    public List<VideoStream> getVideoOnlyStreams() throws IOException, ExtractionException {
        return Collections.emptyList();
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        return StreamType.VIDEO_STREAM;
    }

    @Nullable
    @Override
    public InfoItemsCollector<? extends InfoItem, ? extends InfoItemExtractor> getRelatedItems() throws IOException, ExtractionException {
        return null;
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        final Response response = downloader.get(getLinkHandler().getUrl(), null, LOCALE);
        final Document page = Jsoup.parse(response.responseBody());
        try {
            watch = JsonParser.object().from(
                    page.getElementById("js-initial-watch-data").attr("data-api-data"));
        } catch (JsonParserException e) {
           throw new ExtractionException("could not extract watching page");
        }
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return watch.getObject("video").getString("title");
    }
}
