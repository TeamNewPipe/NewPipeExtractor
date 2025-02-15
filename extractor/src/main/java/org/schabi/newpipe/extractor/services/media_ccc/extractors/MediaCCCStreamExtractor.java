package org.schabi.newpipe.extractor.services.media_ccc.extractors;

import static org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCParsingHelper.getImageListFromLogoImageUrl;
import static org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCParsingHelper.getThumbnailsFromStreamItem;
import static org.schabi.newpipe.extractor.services.media_ccc.extractors.MediaCCCParsingHelper.parseDateFrom;
import static org.schabi.newpipe.extractor.stream.AudioStream.UNKNOWN_BITRATE;
import static org.schabi.newpipe.extractor.stream.Stream.ID_UNKNOWN;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.localization.DateWrapper;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.services.media_ccc.extractors.data.MediaCCCRecording;
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCConferenceLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.media_ccc.linkHandler.MediaCCCStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.schabi.newpipe.extractor.utils.JsonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MediaCCCStreamExtractor extends StreamExtractor {
    private JsonObject data;
    private JsonObject conferenceData;

    public MediaCCCStreamExtractor(final StreamingService service, final LinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Nonnull
    @Override
    public String getTextualUploadDate() {
        return data.getString("release_date");
    }

    @Nonnull
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return new DateWrapper(parseDateFrom(getTextualUploadDate()));
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() {
        return getThumbnailsFromStreamItem(data);
    }

    @Nonnull
    @Override
    public Description getDescription() {
        return new Description(data.getString("description"), Description.PLAIN_TEXT);
    }

    @Override
    public long getLength() {
        return data.getInt("length");
    }

    @Override
    public long getViewCount() {
        return data.getInt("view_count");
    }

    @Nonnull
    @Override
    public String getUploaderUrl() {
        return MediaCCCConferenceLinkHandlerFactory.CONFERENCE_PATH + getUploaderName();
    }

    @Nonnull
    @Override
    public String getUploaderName() {
        return data.getString("conference_url")
                .replaceFirst("https://(api\\.)?media\\.ccc\\.de/public/conferences/", "");
    }

    @Nonnull
    @Override
    public List<Image> getUploaderAvatars() {
        return getImageListFromLogoImageUrl(conferenceData.getString("logo_url"));
    }

    @Override
    public List<AudioStream> getAudioStreams() {
        final List<MediaCCCRecording.Audio> recordings = getRecordings().stream()
                .flatMap(r ->
                        r instanceof MediaCCCRecording.Audio
                                ? Stream.of((MediaCCCRecording.Audio) r)
                                : Stream.empty()
                )
                .collect(Collectors.toList());
        final List<AudioStream> audioStreams = new ArrayList<>();
        for (final MediaCCCRecording.Audio recording : recordings) {
            // First we need to resolve the actual video data from the CDN
            final MediaFormat mediaFormat;
            if (recording.mimeType.endsWith("opus")) {
                mediaFormat = MediaFormat.OPUS;
            } else if (recording.mimeType.endsWith("mpeg")) {
                mediaFormat = MediaFormat.MP3;
            } else if (recording.mimeType.endsWith("ogg")) {
                mediaFormat = MediaFormat.OGG;
            } else {
                mediaFormat = null;
            }
            audioStreams.add(new AudioStream.Builder()
                    .setId(recording.filename)
                    .setContent(recording.url, true)
                    .setMediaFormat(mediaFormat)
                    .setAverageBitrate(UNKNOWN_BITRATE)
                    .setAudioLocale(recording.language)
                    .build());
        }
        return audioStreams;
    }

    @Override
    public List<VideoStream> getVideoStreams() throws ExtractionException {

        final List<MediaCCCRecording.Video> recordings = getRecordings().stream()
                .flatMap(r ->
                        r instanceof MediaCCCRecording.Video
                                ? Stream.of((MediaCCCRecording.Video) r)
                                : Stream.empty()
                )
                .collect(Collectors.toList());
        final List<VideoStream> videoStreams = new ArrayList<>();
        for (final MediaCCCRecording.Video recording : recordings) {
                // First we need to resolve the actual video data from the CDN
                final MediaFormat mediaFormat;
                if (recording.mimeType.endsWith("webm")) {
                    mediaFormat = MediaFormat.WEBM;
                } else if (recording.mimeType.endsWith("mp4")) {
                    mediaFormat = MediaFormat.MPEG_4;
                } else {
                    mediaFormat = null;
                }

                // Not checking containsSimilarStream here, since MediaCCC does not provide enough
                // information to decide whether two streams are similar. Hence that method would
                // always return false, e.g. even for different language variations.
                videoStreams.add(new VideoStream.Builder()
                        .setId(recording.filename)
                        .setContent(recording.url, true)
                        .setIsVideoOnly(false)
                        .setMediaFormat(mediaFormat)
                        .setResolution(recording.height + "p")
                        .build());
        }

        return videoStreams;
    }

    public List<MediaCCCRecording> getRecordings() {
        final JsonArray recordingsArray = data.getArray("recordings");
        final List<MediaCCCRecording> recordings = new ArrayList<>();
        for (int i = 0; i < recordingsArray.size(); i++) {
            final JsonObject recording = recordingsArray.getObject(i);
            final String mimeType = recording.getString("mime_type");
            final String languages = recording.getString("language");
            final String url = recording.getString("recording_url");

            if (mimeType.startsWith("video/")) {
                final MediaCCCRecording.Video v =
                        new MediaCCCRecording.Video();
                final String folder = recording.getString("folder");
                v.filename = recording.getString("filename", ID_UNKNOWN);
                // they will put the slides videos into the "slides" folder
                v.recordingType = folder.contains("slides")
                        ? MediaCCCRecording.VideoType.SLIDES
                        : MediaCCCRecording.VideoType.MAIN;
                v.mimeType = mimeType;
                v.languages = Arrays.stream(languages.split("-"))
                        .map(MediaCCCStreamExtractor::mediaCCCLanguageTagToLocale)
                        .filter(l -> l != null)
                        .collect(Collectors.toList());
                v.url = url;
                v.lengthSeconds = recording.getInt("length");
                v.width = recording.getInt("width");
                v.height = recording.getInt("height");
                recordings.add(v);
                continue;
            }
            if (mimeType.startsWith("audio/")) {
                final MediaCCCRecording.Audio a =
                        new MediaCCCRecording.Audio();
                a.filename = recording.getString("filename", ID_UNKNOWN);
                a.mimeType = mimeType;
                a.language = mediaCCCLanguageTagToLocale(languages);
                a.url = url;
                a.lengthSeconds = recording.getInt("length");
                recordings.add(a);
                continue;
            }
            if (mimeType == "application/x-subrip") {
                final MediaCCCRecording.Subtitle s =
                        new MediaCCCRecording.Subtitle();
                s.filename = recording.getString("filename", ID_UNKNOWN);
                s.mimeType = mimeType;
                s.language = mediaCCCLanguageTagToLocale(languages);
                s.url = url;
                recordings.add(s);
                continue;
            }
            final String folder = recording.getString("folder");
            if (mimeType.startsWith("application/") && folder.contains("slides")) {
                final MediaCCCRecording.Slides s =
                        new MediaCCCRecording.Slides();
                s.filename = recording.getString("filename", ID_UNKNOWN);
                s.mimeType = mimeType;
                s.language = mediaCCCLanguageTagToLocale(languages);
                s.url = url;
                recordings.add(s);
                continue;
            }
            final MediaCCCRecording.Unknown u =
                    new MediaCCCRecording.Unknown();
            u.filename = recording.getString("filename", ID_UNKNOWN);
            u.mimeType = mimeType;
            u.url = url;
            u.rawObject = recording;
            recordings.add(u);
        }

        return recordings;
    }

    /** Translate the media.ccc.de language tag to a Locale.
     * The use the first three letters of the German word for the language.
     * In case there’s still a `-` in the string, we’ll split on the first part.
     * @param language language tag
     * @return null if we don’t have that language in our switch, or Locale
     */
    private static @Nullable Locale mediaCCCLanguageTagToLocale(@Nonnull String language) {
        final int idx = language.indexOf('-');
        if (idx != -1) {
            // TODO: would be cool if we could WARN here, but let’s just continue in case there’s still a separator
            language = language.substring(0, idx);
        }
        switch (language) {
            case "deu":
                return Locale.GERMAN;
            case "eng":
                return Locale.ENGLISH;
            case "fra":
                return Locale.FRENCH;
            case "ita":
                return Locale.ITALIAN;
            case "spa":
                return Locale.forLanguageTag("es");
            default:
                return null;
        }
    }

    @Override
    public List<VideoStream> getVideoOnlyStreams() {
        return Collections.emptyList();
    }

    @Override
    public StreamType getStreamType() {
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        final String videoUrl = MediaCCCStreamLinkHandlerFactory.VIDEO_API_ENDPOINT + getId();
        try {
            data = JsonParser.object().from(downloader.get(videoUrl).responseBody());
            conferenceData = JsonParser.object()
                    .from(downloader.get(data.getString("conference_url")).responseBody());
        } catch (final JsonParserException jpe) {
            throw new ExtractionException("Could not parse json returned by URL: " + videoUrl,
                    jpe);
        }
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return data.getString("title");
    }

    @Nonnull
    @Override
    public String getOriginalUrl() {
        return data.getString("frontend_link");
    }

    @Override
    public Locale getLanguageInfo() throws ParsingException {
        return Localization.getLocaleFromThreeLetterCode(data.getString("original_language"));
    }

    @Nonnull
    @Override
    public List<String> getTags() {
        return JsonUtils.getStringListFromJsonArray(data.getArray("tags"));
    }
}
