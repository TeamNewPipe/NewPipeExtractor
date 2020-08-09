package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import org.jsoup.Jsoup;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ContentNotSupportedException;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.Description;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper.getImageUrl;

public class BandcampRadioStreamExtractor extends BandcampStreamExtractor {

    private JsonObject showInfo;
    private LinkHandler linkHandler;

    public BandcampRadioStreamExtractor(StreamingService service, LinkHandler linkHandler) {
        super(service, linkHandler);
        this.linkHandler = linkHandler;
    }

    static JsonObject query(int id) throws ParsingException {
        try {
            return JsonParser.object().from(
                    NewPipe.getDownloader().get("https://bandcamp.com/api/bcweekly/1/get?id=" + id).responseBody()
            );
        } catch (IOException | ReCaptchaException | JsonParserException e) {
            throw new ParsingException("could not get show data", e);
        }
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        showInfo = query(Integer.parseInt(getId()));
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return showInfo.getString("subtitle"); // "audio_title" is a boring title
    }

    @Nonnull
    @Override
    public String getUploaderUrl() throws ContentNotSupportedException {
        throw new ContentNotSupportedException("Fan pages are not supported");
    }

    @Nonnull
    @Override
    public String getUrl() throws ParsingException {
        return linkHandler.getUrl();
    }

    @Nonnull
    @Override
    public String getUploaderName() {
        return Jsoup.parse(showInfo.getString("image_caption"))
                .getElementsByTag("a").first().text();
    }

    @Nullable
    @Override
    public String getTextualUploadDate() {
        return showInfo.getString("published_date");
    }

    @Nonnull
    @Override
    public String getThumbnailUrl() throws ParsingException {
        return getImageUrl(showInfo.getLong("show_image_id"), false);
    }

    @Nonnull
    @Override
    public String getUploaderAvatarUrl() {
        return "https://bandcamp.com/img/buttons/bandcamp-button-circle-whitecolor-512.png";
    }

    @Nonnull
    @Override
    public Description getDescription() {
        return new Description(showInfo.getString("desc"), Description.PLAIN_TEXT);
    }

    @Override
    public long getLength() {
        return showInfo.getLong("audio_duration");
    }

    @Override
    public List<AudioStream> getAudioStreams() {
        ArrayList<AudioStream> list = new ArrayList<>();
        JsonObject streams = showInfo.getObject("audio_stream");

        if (streams.has("opus-lo")) {
            list.add(new AudioStream(
                    streams.getString("opus-lo"),
                    MediaFormat.OPUS, 100
            ));
        }
        if (streams.has("mp3-128")) {
            list.add(new AudioStream(
                    streams.getString("mp3-128"),
                    MediaFormat.MP3, 128
            ));
        }

        return list;
    }

    @Nonnull
    @Override
    public String getLicence() {
        return "";
    }

    @Nonnull
    @Override
    public String getCategory() {
        return "";
    }

    @Nonnull
    @Override
    public List<String> getTags() {
        return new ArrayList<>();
    }
}
