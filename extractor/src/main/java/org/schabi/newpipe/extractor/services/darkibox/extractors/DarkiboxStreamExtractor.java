package org.schabi.newpipe.extractor.services.darkibox.extractors;

import org.schabi.newpipe.extractor.Image;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.VideoStream;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.utils.Parser;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.schabi.newpipe.extractor.stream.Stream.ID_UNKNOWN;

/**
 * Stream extractor for Darkibox video hosting.
 *
 * <p>The extraction flow is:</p>
 * <ol>
 *     <li>POST to {@code https://darkibox.com/dl} with form data:
 *         {@code op=embed&file_code=FILECODE&auto=1}</li>
 *     <li>Response contains packed JavaScript: {@code eval(function(p,a,c,k,e,d){...})}</li>
 *     <li>After unpacking, the video URL is found in the {@code file:"URL"} parameter
 *         of a PlayerJS configuration</li>
 * </ol>
 */
public class DarkiboxStreamExtractor extends StreamExtractor {

    private static final String DL_URL = "https://darkibox.com/dl";

    private static final Pattern PACKED_JS_PATTERN = Pattern.compile(
            "eval\\(function\\(p,a,c,k,e,d\\)\\{.*?\\}\\('(.*?)',(\\d+),(\\d+),"
                    + "'(.*?)'\\.(split)\\('\\|'\\)",
            Pattern.DOTALL);

    private static final Pattern FILE_URL_PATTERN = Pattern.compile(
            "[\"']?file[\"']?\\s*:\\s*\"(https?://[^\"]+)\"");

    private static final Pattern TITLE_PATTERN = Pattern.compile(
            "<title>([^<]*)</title>", Pattern.CASE_INSENSITIVE);

    private static final Pattern THUMBNAIL_PATTERN = Pattern.compile(
            "[\"']?image[\"']?\\s*:\\s*\"(https?://[^\"]+)\"");

    private String videoUrl;
    private String videoTitle;
    private String thumbnailUrl;

    public DarkiboxStreamExtractor(final StreamingService service,
                                   final LinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        final String fileCode = getId();

        // First, fetch the embed page to get the title
        final String embedUrl = "https://darkibox.com/embed-" + fileCode + ".html";
        final Response embedResponse = downloader.get(embedUrl);
        final String embedPage = embedResponse.responseBody();

        // Try to extract title from embed page
        try {
            final String rawTitle = Parser.matchGroup1(TITLE_PATTERN, embedPage);
            // Clean up common suffixes like " - Darkibox" or "Watch "
            videoTitle = rawTitle
                    .replaceAll("(?i)\\s*[-|]\\s*darkibox.*$", "")
                    .replaceAll("(?i)^watch\\s+", "")
                    .trim();
        } catch (final Parser.RegexException e) {
            videoTitle = fileCode;
        }

        // POST to the dl endpoint with form-encoded data
        final String formData = "op=embed&file_code=" + fileCode + "&auto=1";
        final Map<String, List<String>> headers = new HashMap<>();
        headers.put("Content-Type",
                Collections.singletonList("application/x-www-form-urlencoded"));
        headers.put("Referer", Collections.singletonList(embedUrl));

        final Response response = downloader.post(
                DL_URL, headers, formData.getBytes(StandardCharsets.UTF_8));
        final String responseBody = response.responseBody();

        // Look for packed JS in the response
        final Matcher packedMatcher = PACKED_JS_PATTERN.matcher(responseBody);
        final String scriptContent;
        if (packedMatcher.find()) {
            scriptContent = unpackJs(
                    packedMatcher.group(1),
                    Integer.parseInt(packedMatcher.group(2)),
                    Integer.parseInt(packedMatcher.group(3)),
                    packedMatcher.group(4).split("\\|")
            );
        } else {
            // The response might not be packed, try to use it directly
            scriptContent = responseBody;
        }

        // Extract the video file URL
        try {
            videoUrl = Parser.matchGroup1(FILE_URL_PATTERN, scriptContent);
        } catch (final Parser.RegexException e) {
            throw new ExtractionException(
                    "Could not extract video URL from Darkibox response for file code: "
                            + fileCode, e);
        }

        // Try to extract thumbnail
        try {
            thumbnailUrl = Parser.matchGroup1(THUMBNAIL_PATTERN, scriptContent);
        } catch (final Parser.RegexException e) {
            thumbnailUrl = null;
        }
    }

    /**
     * Unpack JavaScript that has been packed with Dean Edwards' packer.
     *
     * <p>The packer encodes JavaScript by replacing words with short symbols and
     * storing a dictionary. This method reverses that process.</p>
     *
     * @param payload   the packed payload string (the p argument)
     * @param radix     the base/radix used for encoding (the a argument)
     * @param count     the number of words in the dictionary (the c argument)
     * @param keywords  the dictionary of replacement words (the k argument split by |)
     * @return the unpacked JavaScript code
     */
    private static String unpackJs(final String payload,
                                   final int radix,
                                   final int count,
                                   final String[] keywords) {
        // Build the unpacked string by replacing encoded tokens with dictionary words
        final StringBuilder result = new StringBuilder();
        final Pattern tokenPattern = Pattern.compile("\\b(\\w+)\\b");
        final Matcher tokenMatcher = tokenPattern.matcher(payload);
        int lastEnd = 0;

        while (tokenMatcher.find()) {
            result.append(payload, lastEnd, tokenMatcher.start());
            final String token = tokenMatcher.group(1);
            final int index = parseBaseN(token, radix);
            if (index >= 0 && index < keywords.length && !keywords[index].isEmpty()) {
                result.append(keywords[index]);
            } else {
                result.append(token);
            }
            lastEnd = tokenMatcher.end();
        }
        result.append(payload, lastEnd, payload.length());

        return result.toString();
    }

    /**
     * Parse a string as a number in the given base/radix.
     * Supports bases up to 36 (digits 0-9, letters a-z).
     */
    private static int parseBaseN(final String str, final int radix) {
        try {
            return Integer.parseInt(str, radix);
        } catch (final NumberFormatException e) {
            return -1;
        }
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        assertPageFetched();
        return videoTitle != null ? videoTitle : getId();
    }

    @Nonnull
    @Override
    public List<Image> getThumbnails() throws ParsingException {
        assertPageFetched();
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            return List.of(new Image(
                    thumbnailUrl,
                    Image.HEIGHT_UNKNOWN,
                    Image.WIDTH_UNKNOWN,
                    Image.ResolutionLevel.UNKNOWN));
        }
        return List.of();
    }

    @Nonnull
    @Override
    public String getUploaderUrl() {
        return "";
    }

    @Nonnull
    @Override
    public String getUploaderName() {
        return "";
    }

    @Override
    public List<AudioStream> getAudioStreams() {
        return Collections.emptyList();
    }

    @Override
    public List<VideoStream> getVideoStreams() throws ExtractionException {
        assertPageFetched();
        if (videoUrl == null || videoUrl.isEmpty()) {
            return Collections.emptyList();
        }

        final MediaFormat format;
        if (videoUrl.contains(".m3u8")) {
            // HLS streams are handled via getHlsUrl()
            return Collections.emptyList();
        } else if (videoUrl.contains(".mp4")) {
            format = MediaFormat.MPEG_4;
        } else {
            format = MediaFormat.MPEG_4; // default assumption for video hosters
        }

        return List.of(new VideoStream.Builder()
                .setId(ID_UNKNOWN)
                .setContent(videoUrl, true)
                .setIsVideoOnly(false)
                .setMediaFormat(format)
                .setResolution("720p")
                .build());
    }

    @Override
    public List<VideoStream> getVideoOnlyStreams() {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public String getHlsUrl() throws ParsingException {
        assertPageFetched();
        if (videoUrl != null && videoUrl.contains(".m3u8")) {
            return videoUrl;
        }
        return "";
    }

    @Override
    public StreamType getStreamType() {
        return StreamType.VIDEO_STREAM;
    }
}
