// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.Bandcamp;

import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.DefaultStreamExtractorTest;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampStreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.util.Collections;
import java.util.List;

/**
 * Tests for {@link BandcampStreamExtractor}
 */
public class BandcampStreamExtractorTest extends DefaultStreamExtractorTest {

    @Override
    protected StreamExtractor createExtractor() throws Exception {
        return Bandcamp
            .getStreamExtractor("https://teaganbear.bandcamp.com/track/just-for-the-halibut");
    }

    @Override
    public StreamingService expectedService() {
        return Bandcamp;
    }

    @Override
    public String expectedName() {
        return "Just for the Halibut";
    }

    @Override
    public String expectedId() {
        return "https://teaganbear.bandcamp.com/track/just-for-the-halibut";
    }

    @Override
    public String expectedUrlContains() {
        return "https://teaganbear.bandcamp.com/track/just-for-the-halibut";
    }

    @Override
    public String expectedOriginalUrlContains() {
        return "https://teaganbear.bandcamp.com/track/just-for-the-halibut";
    }

    @Override
    public StreamType expectedStreamType() {
        return StreamType.AUDIO_STREAM;
    }

    @Override
    public String expectedUploaderName() {
        return "Teaganbear";
    }

    @Override
    public String expectedUploaderUrl() {
        return "https://teaganbear.bandcamp.com/";
    }

    @Override
    public List<String> expectedDescriptionContains() {
        return Collections.singletonList("it's Creative Commons so feel free to use it in whatever");
    }

    @Override
    public long expectedLength() {
        return 124;
    }

    @Override
    public long expectedViewCountAtLeast() {
        return Long.MIN_VALUE;
    }

    @Override
    public String expectedUploadDate() {
        return "2019-03-10 23:00:42.000";
    }

    @Override
    public String expectedTextualUploadDate() {
        return "10 Mar 2019 23:00:42 GMT";
    }

    @Override
    public long expectedLikeCountAtLeast() {
        return Long.MIN_VALUE;
    }

    @Override
    public long expectedDislikeCountAtLeast() {
        return Long.MIN_VALUE;
    }

    @Override
    public boolean expectedHasVideoStreams() {
        return false;
    }

    @Override
    public boolean expectedHasRelatedItems() {
        return true;
    }

    @Override
    public boolean expectedHasSubtitles() {
        return false;
    }

    @Override
    public boolean expectedHasFrames() {
        return false;
    }

    @Override
    public String expectedLicence() {
        return "CC BY 3.0";
    }

    @Override
    public String expectedCategory() {
        return "dance";
    }

    @Test
    void testArtistProfilePictures() throws ParsingException {
        BandcampTestUtils.testImages(extractor().getUploaderAvatars());
    }

    @Test
    void testTranslateIdsToUrl() throws ParsingException {
        // To add tests: look at website's source, search for `band_id` and `item_id`
        assertEquals(
                "https://teaganbear.bandcamp.com/track/just-for-the-halibut",
                BandcampExtractorHelper.getStreamUrlFromIds(3877364987L, 3486455278L, "track")
        );
    }

}
