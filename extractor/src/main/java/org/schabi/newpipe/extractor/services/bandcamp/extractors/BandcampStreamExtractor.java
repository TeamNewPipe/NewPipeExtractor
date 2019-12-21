// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.extractors;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.schabi.newpipe.extractor.MediaFormat;
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
import java.util.List;

public class BandcampStreamExtractor extends StreamExtractor {

    private JSONObject albumJson;
    private JSONObject current;
    private Document document;

    public BandcampStreamExtractor(StreamingService service, LinkHandler linkHandler) {
        super(service, linkHandler);
    }


    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        String html = downloader.get(getLinkHandler().getUrl()).responseBody();
        document = Jsoup.parse(html);
        albumJson = getAlbumInfoJson(html);
        current = albumJson.getJSONObject("current");

        if (albumJson.getJSONArray("trackinfo").length() > 1) {
            // In this case, we are actually viewing an album page!
            throw new ExtractionException("Page is actually an album, not a track");
        }
    }

    /**
     * Get the JSON that contains album's metadata from page
     *
     * @param html Website
     * @return Album metadata JSON
     * @throws ParsingException    In case of a faulty website
     */
    public static JSONObject getAlbumInfoJson(String html) throws ParsingException {
        try {
            return BandcampExtractorHelper.getJSONFromJavaScriptVariables(html, "TralbumData");
        } catch (JSONException e) {
            throw new ParsingException("Faulty JSON", e);
        }
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return current.getString("title");
    }

    @Nonnull
    @Override
    public String getUploaderUrl() throws ParsingException {
        String[] parts = getUrl().split("/");
        // https: (/) (/) * .bandcamp.com (/) and leave out the rest
        return "https://" + parts[2] + "/";
    }

    @Nonnull
    @Override
    public String getUrl() throws ParsingException {
        return albumJson.getString("url").replace("http://", "https://");
    }

    @Nonnull
    @Override
    public String getUploaderName() throws ParsingException {
        return albumJson.getString("artist");
    }

    @Nullable
    @Override
    public String getTextualUploadDate() throws ParsingException {
        return current.getString("release_date");
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() {
        return null;
    }

    @Nonnull
    @Override
    public String getThumbnailUrl() throws ParsingException {
        return document.getElementsByAttributeValue("property", "og:image").get(0).attr("content");
    }

    @Nonnull
    @Override
    public String getUploaderAvatarUrl() {
        return document.getElementsByClass("band-photo").first().attr("src");
    }

    @Nonnull
    @Override
    public String getDescription() {
        return BandcampExtractorHelper.smartConcatenate(
                new String[]{
                        getStringOrNull(current, "about"),
                        getStringOrNull(current, "lyrics"),
                        getStringOrNull(current, "credits")
                }, "\n\n"
        );
    }

    /**
     * Avoid exceptions like "<code>JSONObject["about"] not a string.</code>" and instead just return null.
     * This is for the case that the actual JSON has something like <code>"about": null</code>.
     */
    private String getStringOrNull(JSONObject jsonObject, String value) {
        try {
            return jsonObject.getString(value);
        } catch (JSONException e) {
            return null;
        }
    }

    @Override
    public int getAgeLimit() throws ParsingException {
        return 0;
    }

    @Override
    public long getLength() throws ParsingException {
        return 0;
    }

    @Override
    public long getTimeStamp() throws ParsingException {
        return 0;
    }

    @Override
    public long getViewCount() throws ParsingException {
        return -1;
    }

    @Override
    public long getLikeCount() throws ParsingException {
        return -1;
    }

    @Override
    public long getDislikeCount() throws ParsingException {
        return -1;
    }

    @Nonnull
    @Override
    public String getDashMpdUrl() throws ParsingException {
        return null;
    }

    @Nonnull
    @Override
    public String getHlsUrl() throws ParsingException {
        return null;
    }

    @Override
    public List<AudioStream> getAudioStreams() throws IOException, ExtractionException {
        return null;
    }

    @Override
    public List<VideoStream> getVideoStreams() throws IOException, ExtractionException {
        return null;
    }

    @Override
    public List<VideoStream> getVideoOnlyStreams() throws IOException, ExtractionException {
        return null;
    }

    @Nonnull
    @Override
    public List<SubtitlesStream> getSubtitlesDefault() throws IOException, ExtractionException {
        return null;
    }

    @Nonnull
    @Override
    public List<SubtitlesStream> getSubtitles(MediaFormat format) throws IOException, ExtractionException {
        return null;
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        return null;
    }

    @Override
    public StreamInfoItem getNextStream() throws IOException, ExtractionException {
        return null;
    }

    @Override
    public StreamInfoItemsCollector getRelatedStreams() throws IOException, ExtractionException {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }
}
