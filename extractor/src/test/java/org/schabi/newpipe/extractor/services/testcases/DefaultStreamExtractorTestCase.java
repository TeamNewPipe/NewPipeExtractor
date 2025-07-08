package org.schabi.newpipe.extractor.services.testcases;

import static org.schabi.newpipe.extractor.stream.StreamExtractor.UNKNOWN_SUBSCRIBER_COUNT;
import org.schabi.newpipe.extractor.MetaInfo;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

import javax.annotation.Nullable;

/**
 * Test case base class for {@link org.schabi.newpipe.extractor.services.DefaultStreamExtractorTest}<p>
 * Ideally you will supply a regex matcher that the url that will automatically parse
 * certain values for the tests.<p>
 * Ones that can't be derived from the url should be overridden in the test case.
 */
public interface DefaultStreamExtractorTestCase extends DefaultExtractorTestCase {
    /**
     * Returns matcher for the URL<p>
     * Implementations should throw IllegalArgumentException if the pattern does not match
     */
    Matcher urlMatcher();

    default String getGroupFromUrl(String groupName) {
        return urlMatcher().group(groupName);
    }

    default int getGroupEndIndexFromUrl(String groupName) {
        return urlMatcher().end(groupName);
    }

    default String id() { return getGroupFromUrl("id"); }

    default String uploader() { return getGroupFromUrl("uploader"); }
    
    StreamType streamType();
    String uploaderName();
    default String uploaderUrl() {
        final int groupEndIndex = getGroupEndIndexFromUrl("uploader");
        if (groupEndIndex < 0) {
            return ""; // no uploader group found in url
        }
        return url().substring(0, groupEndIndex);
    }
    default boolean uploaderVerified() { return false; }
    default long uploaderSubscriberCountAtLeast() { return UNKNOWN_SUBSCRIBER_COUNT; } // default: unknown
    default String subChannelName() { return ""; } // default: no subchannel
    default String subChannelUrl() { return ""; } // default: no subchannel
    default boolean descriptionIsEmpty() { return false; } // default: description is not empty
    List<String> descriptionContains();
    long length();
    default int timestamp() { return 0; } // default: there is no timestamp
    long viewCountAtLeast();

    @Nullable
    String uploadDate(); // format: yyyy-MM-dd HH:mm:ss.SSS
    @Nullable
    String textualUploadDate();
    long likeCountAtLeast();
    long dislikeCountAtLeast();
    default boolean hasRelatedItems() { return true; } // default: there are related videos
    default int ageLimit() { return StreamExtractor.NO_AGE_LIMIT; } // default: no limit
    @Nullable
    default String errorMessage() { return null; } // default: no error message
    default boolean hasVideoStreams() { return true; } // default: there are video streams
    default boolean hasAudioStreams() { return true; } // default: there are audio streams
    default boolean hasSubtitles() { return true; } // default: there are subtitles streams
    @Nullable
    default String dashMpdUrlContains() { return null; } // default: no dash mpd
    default boolean hasFrames() { return true; } // default: there are frames
    @Nullable
    default String host() { return ""; } // default: no host for centralized platforms
    @Nullable
    default StreamExtractor.Privacy privacy() { return StreamExtractor.Privacy.PUBLIC; } // default: public
    default String category() { return ""; } // default: no category
    default String licence() { return ""; } // default: no licence
    @Nullable
    default Locale languageInfo() { return null; } // default: no language info available
    @Nullable
    default List<String> tags() { return Collections.emptyList(); } // default: no tags
    @Nullable
    default String supportInfo() { return ""; } // default: no support info available
    default int streamSegmentsCount() { return -1; } // return 0 or greater to test (default is -1 to ignore)
    @Nullable
    default List<MetaInfo> metaInfo() { return Collections.emptyList(); } // default: no metadata info available
}
