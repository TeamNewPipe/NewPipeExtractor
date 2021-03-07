package org.schabi.newpipe.extractor.services.youtube.stream;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.schabi.newpipe.downloader.DownloaderFactory;
import org.schabi.newpipe.extractor.MetaInfo;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.GeographicRestrictionException;
import org.schabi.newpipe.extractor.exceptions.PaidContentException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.exceptions.PrivateContentException;
import org.schabi.newpipe.extractor.exceptions.YoutubeMusicPremiumContentException;
import org.schabi.newpipe.extractor.services.DefaultStreamExtractorTest;
import org.schabi.newpipe.extractor.services.youtube.YoutubeParsingHelper;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamSegment;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.utils.Utils.EMPTY_STRING;

/*
 * Created by Christian Schabesberger on 30.12.15.
 *
 * Copyright (C) Christian Schabesberger 2015 <chris.schabesberger@mailbox.org>
 * YoutubeVideoExtractorDefault.java is part of NewPipe.
 *
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */
public class YoutubeStreamExtractorDefaultTest {
    private static final String RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/stream/";
    static final String BASE_URL = "https://www.youtube.com/watch?v=";

    public static class NotAvailable {
        @BeforeClass
        public static void setUp() throws IOException {
            YoutubeParsingHelper.resetClientVersionAndKey();
            NewPipe.init(new DownloaderFactory().getDownloader(RESOURCE_PATH + "notAvailable"));
        }

        @Test(expected = GeographicRestrictionException.class)
        public void geoRestrictedContent() throws Exception {
            final StreamExtractor extractor =
                    YouTube.getStreamExtractor(BASE_URL + "_PL2HJKxnOM");
            extractor.fetchPage();
        }

        @Test(expected = ContentNotAvailableException.class)
        public void nonExistentFetch() throws Exception {
            final StreamExtractor extractor =
                    YouTube.getStreamExtractor(BASE_URL + "don-t-exist");
            extractor.fetchPage();
        }

        @Test(expected = ParsingException.class)
        public void invalidId() throws Exception {
            final StreamExtractor extractor =
                    YouTube.getStreamExtractor(BASE_URL + "INVALID_ID_INVALID_ID");
            extractor.fetchPage();
        }

        @Test(expected = PaidContentException.class)
        public void paidContent() throws Exception {
            final StreamExtractor extractor =
                    YouTube.getStreamExtractor(BASE_URL + "ayI2iBwGdxw");
            extractor.fetchPage();
        }

        @Test(expected = PrivateContentException.class)
        public void privateContent() throws Exception {
            final StreamExtractor extractor =
                    YouTube.getStreamExtractor(BASE_URL + "8VajtrESJzA");
            extractor.fetchPage();
        }

        @Test(expected = YoutubeMusicPremiumContentException.class)
        public void youtubeMusicPremiumContent() throws Exception {
            final StreamExtractor extractor =
                    YouTube.getStreamExtractor(BASE_URL + "sMJ8bRN2dak");
            extractor.fetchPage();
        }
    }

    public static class DescriptionTestPewdiepie extends DefaultStreamExtractorTest {
        private static final String ID = "7PIMiDcwNvc";
        private static final int TIMESTAMP = 17;
        private static final String URL = BASE_URL + ID + "&t=" + TIMESTAMP;
        private static StreamExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            YoutubeParsingHelper.resetClientVersionAndKey();
            NewPipe.init(new DownloaderFactory().getDownloader(RESOURCE_PATH + "pewdiwpie"));
            extractor = YouTube.getStreamExtractor(URL);
            extractor.fetchPage();
        }

        // @formatter:off
        @Override public StreamExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return "Marzia & Felix - Wedding 19.08.2019"; }
        @Override public String expectedId() { return ID; }
        @Override public String expectedUrlContains() { return BASE_URL + ID; }
        @Override public String expectedOriginalUrlContains() { return URL; }

        @Override public StreamType expectedStreamType() { return StreamType.VIDEO_STREAM; }
        @Override public String expectedUploaderName() { return "PewDiePie"; }
        @Override public String expectedUploaderUrl() { return "https://www.youtube.com/channel/UC-lHJZR3Gqxm24_Vd_AJ5Yw"; }
        @Override public List<String> expectedDescriptionContains() {
            return Arrays.asList("https://www.youtube.com/channel/UC7l23W7gFi4Uho6WSzckZRA",
                    "https://www.handcraftpictures.com/");
        }
        @Override public boolean expectedUploaderVerified() { return true; }
        @Override public long expectedLength() { return 381; }
        @Override public long expectedTimestamp() { return TIMESTAMP; }
        @Override public long expectedViewCountAtLeast() { return 26682500; }
        @Nullable @Override public String expectedUploadDate() { return "2019-08-24 00:00:00.000"; }
        @Nullable @Override public String expectedTextualUploadDate() { return "2019-08-24"; }
        @Override public long expectedLikeCountAtLeast() { return 5212900; }
        @Override public long expectedDislikeCountAtLeast() { return 30600; }
        @Override public int expectedStreamSegmentsCount() { return 0; }
        // @formatter:on
    }

    public static class DescriptionTestUnboxing extends DefaultStreamExtractorTest {
        private static final String ID = "cV5TjZCJkuA";
        private static final String URL = BASE_URL + ID;
        private static StreamExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            YoutubeParsingHelper.resetClientVersionAndKey();
            NewPipe.init(new DownloaderFactory().getDownloader(RESOURCE_PATH + "unboxing"));
            extractor = YouTube.getStreamExtractor(URL);
            extractor.fetchPage();
        }

        // @formatter:off
        @Override public StreamExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return "This Smartphone Changes Everything..."; }
        @Override public String expectedId() { return ID; }
        @Override public String expectedUrlContains() { return URL; }
        @Override public String expectedOriginalUrlContains() { return URL; }

        @Override public StreamType expectedStreamType() { return StreamType.VIDEO_STREAM; }
        @Override public String expectedUploaderName() { return "Unbox Therapy"; }
        @Override public String expectedUploaderUrl() { return "https://www.youtube.com/channel/UCsTcErHg8oDvUnTzoqsYeNw"; }
        @Override public List<String> expectedDescriptionContains() {
            return Arrays.asList("https://www.youtube.com/watch?v=X7FLCHVXpsA&list=PL7u4lWXQ3wfI_7PgX0C-VTiwLeu0S4v34",
                    "https://www.youtube.com/watch?v=Lqv6G0pDNnw&list=PL7u4lWXQ3wfI_7PgX0C-VTiwLeu0S4v34",
                    "https://www.youtube.com/watch?v=XxaRBPyrnBU&list=PL7u4lWXQ3wfI_7PgX0C-VTiwLeu0S4v34",
                    "https://www.youtube.com/watch?v=U-9tUEOFKNU&list=PL7u4lWXQ3wfI_7PgX0C-VTiwLeu0S4v34");
        }
        @Override public long expectedLength() { return 434; }
        @Override public long expectedViewCountAtLeast() { return 21229200; }
        @Nullable @Override public String expectedUploadDate() { return "2018-06-19 00:00:00.000"; }
        @Nullable @Override public String expectedTextualUploadDate() { return "2018-06-19"; }
        @Override public long expectedLikeCountAtLeast() { return 340100; }
        @Override public long expectedDislikeCountAtLeast() { return 18700; }
        @Override public boolean expectedUploaderVerified() { return true; }
        // @formatter:on
    }

    @Ignore("Test broken, video was made private")
    public static class RatingsDisabledTest extends DefaultStreamExtractorTest {
        private static final String ID = "HRKu0cvrr_o";
        private static final int TIMESTAMP = 17;
        private static final String URL = BASE_URL + ID + "&t=" + TIMESTAMP;
        private static StreamExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            YoutubeParsingHelper.resetClientVersionAndKey();
            NewPipe.init(new DownloaderFactory().getDownloader(RESOURCE_PATH + "ratingsDisabled"));
            extractor = YouTube.getStreamExtractor(URL);
            extractor.fetchPage();
        }

        // @formatter:off
        @Override public StreamExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return "AlphaOmegaSin Fanboy Logic: Likes/Dislikes Disabled = Point Invalid Lol wtf?"; }
        @Override public String expectedId() { return ID; }
        @Override public String expectedUrlContains() { return BASE_URL + ID; }
        @Override public String expectedOriginalUrlContains() { return URL; }

        @Override public StreamType expectedStreamType() { return StreamType.VIDEO_STREAM; }
        @Override public String expectedUploaderName() { return "YouTuber PrinceOfFALLEN"; }
        @Override public String expectedUploaderUrl() { return "https://www.youtube.com/channel/UCQT2yul0lr6Ie9qNQNmw-sg"; }
        @Override public List<String> expectedDescriptionContains() { return Arrays.asList("dislikes", "Alpha", "wrong"); }
        @Override public long expectedLength() { return 84; }
        @Override public long expectedTimestamp() { return TIMESTAMP; }
        @Override public long expectedViewCountAtLeast() { return 190; }
        @Nullable @Override public String expectedUploadDate() { return "2019-01-02 00:00:00.000"; }
        @Nullable @Override public String expectedTextualUploadDate() { return "2019-01-02"; }
        @Override public long expectedLikeCountAtLeast() { return -1; }
        @Override public long expectedDislikeCountAtLeast() { return -1; }
        // @formatter:on
    }

    public static class StreamSegmentsTestOstCollection extends DefaultStreamExtractorTest {
        // StreamSegment example with single macro-makers panel
        private static final String ID = "2RYrHwnLHw0";
        private static final String URL = BASE_URL + ID;
        private static StreamExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            YoutubeParsingHelper.resetClientVersionAndKey();
            NewPipe.init(new DownloaderFactory().getDownloader(RESOURCE_PATH + "streamSegmentsOstCollection"));
            extractor = YouTube.getStreamExtractor(URL);
            extractor.fetchPage();
        }

        // @formatter:off
        @Override public StreamExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return "1 Hour - Most Epic Anime Mix - Battle Anime OST"; }
        @Override public String expectedId() { return ID; }
        @Override public String expectedUrlContains() { return BASE_URL + ID; }
        @Override public String expectedOriginalUrlContains() { return URL; }

        @Override public StreamType expectedStreamType() { return StreamType.VIDEO_STREAM; }
        @Override public String expectedUploaderName() { return "MathCaires"; }
        @Override public String expectedUploaderUrl() { return "https://www.youtube.com/channel/UChFoHg6IT18SCqiwCp_KY7Q"; }
        @Override public List<String> expectedDescriptionContains() {
            return Arrays.asList("soundtracks", "9:49", "YouSeeBIGGIRLTT");
        }
        @Override public long expectedLength() { return 3889; }
        @Override public long expectedViewCountAtLeast() { return 2463261; }
        @Nullable @Override public String expectedUploadDate() { return "2019-06-26 00:00:00.000"; }
        @Nullable @Override public String expectedTextualUploadDate() { return "2019-06-26"; }
        @Override public long expectedLikeCountAtLeast() { return 32100; }
        @Override public long expectedDislikeCountAtLeast() { return 750; }
        @Override public boolean expectedHasSubtitles() { return false; }

        @Override public int expectedStreamSegmentsCount() { return 17; }
        @Test
        public void testStreamSegment() throws Exception {
            final StreamSegment segment = extractor.getStreamSegments().get(3);
            assertEquals(589, segment.getStartTimeSeconds());
            assertEquals("Attack on Titan S2 - YouSeeBIGGIRLTT", segment.getTitle());
            assertEquals(BASE_URL + ID + "?t=589", segment.getUrl());
            assertNotNull(segment.getPreviewUrl());
        }
        // @formatter:on
    }

    public static class StreamSegmentsTestMaiLab extends DefaultStreamExtractorTest {
        // StreamSegment example with macro-makers panel and transcription panel
        private static final String ID = "ud9d5cMDP_0";
        private static final String URL = BASE_URL + ID;
        private static StreamExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            YoutubeParsingHelper.resetClientVersionAndKey();
            NewPipe.init(new DownloaderFactory().getDownloader(RESOURCE_PATH + "streamSegmentsMaiLab"));
            extractor = YouTube.getStreamExtractor(URL);
            extractor.fetchPage();
        }

        // @formatter:off
        @Override public StreamExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return "Vitamin D wissenschaftlich gepr\u00fcft"; }
        @Override public String expectedId() { return ID; }
        @Override public String expectedUrlContains() { return BASE_URL + ID; }
        @Override public String expectedOriginalUrlContains() { return URL; }

        @Override public StreamType expectedStreamType() { return StreamType.VIDEO_STREAM; }
        @Override public String expectedUploaderName() { return "maiLab"; }
        @Override public String expectedUploaderUrl() { return "https://www.youtube.com/channel/UCyHDQ5C6z1NDmJ4g6SerW8g"; }
        @Override public List<String> expectedDescriptionContains()  {return Arrays.asList("Vitamin", "2:44", "Was ist Vitamin D?");}
        @Override public boolean expectedUploaderVerified() { return true; }
        @Override public long expectedLength() { return 1010; }
        @Override public long expectedViewCountAtLeast() { return 815500; }
        @Nullable @Override public String expectedUploadDate() { return "2020-11-18 00:00:00.000"; }
        @Nullable @Override public String expectedTextualUploadDate() { return "2020-11-18"; }
        @Override public long expectedLikeCountAtLeast() { return 48500; }
        @Override public long expectedDislikeCountAtLeast() { return 20000; }
        @Override public boolean expectedHasSubtitles() { return true; }
        @Override public int expectedStreamSegmentsCount() { return 7; }
        // @formatter:on

        @Test
        public void testStreamSegment() throws Exception {
            final StreamSegment segment = extractor.getStreamSegments().get(1);
            assertEquals(164, segment.getStartTimeSeconds());
            assertEquals("Was ist Vitamin D?", segment.getTitle());
            assertEquals(BASE_URL + ID + "?t=164", segment.getUrl());
            assertNotNull(segment.getPreviewUrl());
        }

        @Override
        @Test
        @Ignore("encoding problem")
        public void testName() throws Exception {
            super.testName();
        }
    }

    public static class PublicBroadcasterTest extends DefaultStreamExtractorTest {
        private static final String ID = "q6fgbYWsMgw";
        private static final int TIMESTAMP = 0;
        private static final String URL = BASE_URL + ID;
        private static StreamExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            YoutubeParsingHelper.resetClientVersionAndKey();
            NewPipe.init(new DownloaderFactory().getDownloader(RESOURCE_PATH + "publicBroadcast"));
            extractor = YouTube.getStreamExtractor(URL);
            extractor.fetchPage();
        }

        // @formatter:off
        @Override public StreamExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return "Was verbirgt sich am tiefsten Punkt des Ozeans?"; }
        @Override public String expectedId() { return ID; }
        @Override public String expectedUrlContains() { return BASE_URL + ID; }
        @Override public String expectedOriginalUrlContains() { return URL; }

        @Override public StreamType expectedStreamType() { return StreamType.VIDEO_STREAM; }
        @Override public String expectedUploaderName() { return "Dinge Erklärt – Kurzgesagt"; }
        @Override public String expectedUploaderUrl() { return "https://www.youtube.com/channel/UCwRH985XgMYXQ6NxXDo8npw"; }
        @Override public List<String> expectedDescriptionContains() { return Arrays.asList("Lasst uns abtauchen!", "Angebot von funk", "Dinge"); }
        @Override public long expectedLength() { return 631; }
        @Override public long expectedTimestamp() { return TIMESTAMP; }
        @Override public long expectedViewCountAtLeast() { return 1_600_000; }
        @Nullable @Override public String expectedUploadDate() { return "2019-06-12 00:00:00.000"; }
        @Nullable @Override public String expectedTextualUploadDate() { return "2019-06-12"; }
        @Override public long expectedLikeCountAtLeast() { return 70000; }
        @Override public long expectedDislikeCountAtLeast() { return 500; }
        @Override public List<MetaInfo> expectedMetaInfo() throws MalformedURLException {
            return Collections.singletonList(new MetaInfo(
                    EMPTY_STRING,
                    new Description("Funk is a German public broadcast service.", Description.PLAIN_TEXT),
                    Collections.singletonList(new URL("https://de.wikipedia.org/wiki/Funk_(Medienangebot)?wprov=yicw1")),
                    Collections.singletonList("Wikipedia (German)")
            ));
        }
        @Override public boolean expectedUploaderVerified() { return true; }
        // @formatter:on
    }

}
