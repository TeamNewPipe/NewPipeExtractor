package org.schabi.newpipe.extractor.services.testcases;

import org.immutables.value.Value;
import org.schabi.newpipe.extractor.ImmutableStyle;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.services.soundcloud.SoundcloudStreamExtractorTest;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.utils.Parser;
import org.schabi.newpipe.extractor.utils.Parser.RegexException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;

// CHECKSTYLE:OFF
/**
 * Immutable definition of {@link DefaultStreamExtractorTestCase}
 * for {@link SoundcloudStreamExtractorTest} streams
 * @see SoundcloudStreamExtractorTest
 */
// CHECKSTYLE:ON
@ImmutableStyle
@Value.Immutable
public interface ISoundcloudStreamExtractorTestCase extends DefaultStreamExtractorTestCase {

    /**
     * Pattern for matching soundcloud stream URLs
     * Matches URLs of the form:
     * <pre>
     * <a href="https://soundcloud.com/user-904087338/nether#t=46">...</a>
     * </pre>
     */
    Pattern URL_PATTERN = Pattern.compile(
        "^https?://(?:www\\.|m\\.|on\\.)?soundcloud\\.com/"
        + "(?<uploader>[0-9a-z_-]+)/(?!(?:tracks|albums|sets|reposts|followers|following)/?$)"
        + "(?<id>[0-9a-z_-]+)/?"
        + "([#?](t=(?<timestamp>\\d+)|.*))?$"
    );

    /**
     * Returns the named group from the URL, or an empty string if not found.
     */
    default String getGroupFromUrl(String group) {
        try {
            final String value = urlMatcher().group(group);
            return value != null ? value : "";
        } catch (IllegalArgumentException | IllegalStateException e) {
            return "";
        }
    }

    /**
     * Returns the end index of the named group from the URL, or -1 if not found.
     */
    default int getGroupEndIndexFromUrl(String group) {
        try {
            return urlMatcher().end(group);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return -1;
        }
    }

    default Matcher urlMatcher() {
        try {
            return Parser.matchOrThrow(URL_PATTERN, url());
        } catch (RegexException e) {
            throw new IllegalArgumentException("URL does not match expected SoundCloud pattern: " + url(), e);
        }
    }

    default String urlContains() {
        final int groupEndIndex = getGroupEndIndexFromUrl("id");
        if (groupEndIndex < 0) {
            return url(); // no id group found in url
        }
        return url().substring(0, groupEndIndex);
    }

    @Value.Derived
    default StreamingService service() { return SoundCloud; }

    @Value.Derived
    @Override
    default StreamType streamType() { return StreamType.AUDIO_STREAM; }
    
    @Override
    default int timestamp() {
        try {
            return Integer.parseInt(getGroupFromUrl("timestamp"));
        } 
        catch (NumberFormatException e) {
            // Return 0 if no timestamp
            return 0;
        } 
    }
    
    @Override
    default long dislikeCountAtLeast() { return -1; } // default: soundcloud has no dislikes

    @Override
    default boolean hasVideoStreams() { return false; } // default: soundcloud has no video streams
    
    @Override
    default boolean hasSubtitles() { return false; } // default: soundcloud has no subtitles
    
    default boolean hasFrames() { return false; } // default: soundcloud has no frames

    default int streamSegmentsCount() { return 0; }
}
