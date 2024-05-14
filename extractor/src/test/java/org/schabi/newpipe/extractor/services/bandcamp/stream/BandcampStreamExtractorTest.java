// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package org.schabi.newpipe.extractor.services.bandcamp.stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderFactory;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.DefaultStreamExtractorTest;
import org.schabi.newpipe.extractor.services.bandcamp.BandcampTestUtils;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampExtractorHelper;
import org.schabi.newpipe.extractor.services.bandcamp.extractors.BandcampStreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.Bandcamp;

/**
 * Tests for {@link BandcampStreamExtractor}
 */
public class BandcampStreamExtractorTest extends DefaultStreamExtractorTest {

    private static final String RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/bandcamp/extractor/stream/";
    private static BandcampStreamExtractor extractor;

    @BeforeAll
    public static void setUp() throws ExtractionException, IOException {
        NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH));

        extractor = (BandcampStreamExtractor) Bandcamp
                .getStreamExtractor("https://teaganbear.bandcamp.com/track/just-for-the-halibut");
        extractor.fetchPage();
    }

    @Override
    public StreamExtractor extractor() {
        return extractor;
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
    void testArtistProfilePictures() {
        BandcampTestUtils.testImages(extractor.getUploaderAvatars());
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
