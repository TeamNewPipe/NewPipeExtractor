package org.schabi.newpipe.extractor.stream;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.utils.ExtractorLogger;
import org.schabi.newpipe.extractor.utils.Parser;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

public final class SoundcloudHlsUtils {
    private static final String TAG = HlsAudioStream.class.getSimpleName();
    private static final Pattern MP3_HLS_PATTERN =
            Pattern.compile("https://cf-hls-media\\.sndcdn.com/playlist/"
                            + "([a-zA-Z0-9]+)\\.128\\.mp3/playlist\\.m3u8");
    private static final Pattern AAC_HLS_PATTERN =
            Pattern.compile("https://playback\\.media-streaming\\.soundcloud\\.cloud/"
                            + "([a-zA-Z0-9]+)/aac_160k/[a-f0-9\\-]+/playlist\\.m3u8");
    private static final Pattern OPUS_HLS_PATTERN =
            Pattern.compile("https://cf-hls-opus-media\\.sndcdn\\.com/"
                            + "playlist/([a-zA-Z0-9]+)\\.64\\.opus/playlist\\.m3u8");

    private SoundcloudHlsUtils() { }

    /**
     * Calls the API endpoint url for this stream to get the url for retrieving the
     * actual byte data for playback (returns the m3u8 playlist url for HLS streams,
     * and the url to get the full binary track for progressives streams)<p>
     *
     * NOTE: this returns a different url every time! (for SoundCloud)
     * @param apiStreamUrl The url to call to get the actual stream data url
     * @return The url for playing the audio (e.g. playlist.m3u8)
     * @throws IOException If there's a problem calling the endpoint
     * @throws ExtractionException for the same reason
     */
    public static String getStreamContentUrl(final String apiStreamUrl)
            throws IOException, ExtractionException {
        ExtractorLogger.d(TAG, "Fetching content url for {url}", apiStreamUrl);
        final String response = NewPipe.getDownloader()
                                       .get(apiStreamUrl)
                                       .validateResponseCode()
                                       .responseBody();
        final JsonObject urlObject;
        try {
            urlObject = JsonParser.object().from(response);
        } catch (final JsonParserException e) {
            // TODO: Improve error message.
            throw new ParsingException("Could not parse stream content from URL ("
                                       + response + ")", e);
        }

        return urlObject.getString("url");
    }

    @Nonnull
    public static String extractHlsPlaylistId(final String hlsPlaylistUrl,
                                              final MediaFormat mediaFormat)
            throws ExtractionException {
        switch (mediaFormat) {
            case MP3: return extractHlsMp3PlaylistId(hlsPlaylistUrl);
            case M4A: return extractHlsAacPlaylistId(hlsPlaylistUrl);
            case OPUS: return extractHlsOpusPlaylistId(hlsPlaylistUrl);
            default:
                    throw new IllegalArgumentException("Unsupported media format: " + mediaFormat);
        }
    }

    private static String extractHlsMp3PlaylistId(final String hlsPlaylistUrl)
            throws ExtractionException {
        return Parser.matchGroup1(MP3_HLS_PATTERN, hlsPlaylistUrl);
    }

    private static String extractHlsAacPlaylistId(final String hlsPlaylistUrl)
            throws ExtractionException {
        return Parser.matchGroup1(AAC_HLS_PATTERN, hlsPlaylistUrl);
    }

    private static String extractHlsOpusPlaylistId(final String hlsPlaylistUrl)
            throws ExtractionException {
        return Parser.matchGroup1(OPUS_HLS_PATTERN, hlsPlaylistUrl);
    }
}
