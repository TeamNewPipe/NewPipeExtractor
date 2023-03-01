/*
 * Created by Christian Schabesberger on 30.12.15.
 *
 * Copyright (C) Christian Schabesberger 2015 <chris.schabesberger@mailbox.org>
 * YoutubeVideoExtractorDefault.java is part of NewPipe Extractor.
 *
 * NewPipe Extractor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe Extractor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe Extractor. If not, see <https://www.gnu.org/licenses/>.
 */

package org.schabi.newpipe.extractor.services.youtube.stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderFactory;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
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
import org.schabi.newpipe.extractor.services.youtube.YoutubeTestsUtils;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.Description;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamSegment;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.utils.LocaleCompat;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nullable;

public class YoutubeStreamExtractorDefaultTest {
    private static final String RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/stream/";
    static final String BASE_URL = "https://www.youtube.com/watch?v=";
    public static final String YOUTUBE_LICENCE = "YouTube licence";

    public static class NotAvailable {
        @BeforeAll
        public static void setUp() throws IOException {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "notAvailable"));
        }

        @Test
        void geoRestrictedContent() throws Exception {
            final StreamExtractor extractor =
                    YouTube.getStreamExtractor(BASE_URL + "_PL2HJKxnOM");
            assertThrows(GeographicRestrictionException.class, extractor::fetchPage);
        }

        @Test
        void nonExistentFetch() throws Exception {
            final StreamExtractor extractor =
                    YouTube.getStreamExtractor(BASE_URL + "don-t-exist");
            assertThrows(ContentNotAvailableException.class, extractor::fetchPage);
        }

        @Test
        void invalidId() throws Exception {
            final StreamExtractor extractor =
                    YouTube.getStreamExtractor(BASE_URL + "INVALID_ID_INVALID_ID");
            assertThrows(ParsingException.class, extractor::fetchPage);
        }

        @Test
        void paidContent() throws Exception {
            final StreamExtractor extractor =
                    YouTube.getStreamExtractor(BASE_URL + "ayI2iBwGdxw");
            assertThrows(PaidContentException.class, extractor::fetchPage);
        }

        @Test
        void privateContent() throws Exception {
            final StreamExtractor extractor =
                    YouTube.getStreamExtractor(BASE_URL + "8VajtrESJzA");
            assertThrows(PrivateContentException.class, extractor::fetchPage);
        }

        @Test
        void youtubeMusicPremiumContent() throws Exception {
            final StreamExtractor extractor =
                    YouTube.getStreamExtractor(BASE_URL + "sMJ8bRN2dak");
            assertThrows(YoutubeMusicPremiumContentException.class, extractor::fetchPage);
        }
    }

    public static class DescriptionTestPewdiepie extends DefaultStreamExtractorTest {
        private static final String ID = "7PIMiDcwNvc";
        private static final int TIMESTAMP = 7483;
        private static final String URL = BASE_URL + ID + "&t=" + TIMESTAMP + "s";
        private static StreamExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "pewdiwpie"));
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
        @Override public long expectedUploaderSubscriberCountAtLeast() { return 110_000_000; }
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
        @Override public long expectedDislikeCountAtLeast() { return -1; }
        @Override public int expectedStreamSegmentsCount() { return 0; }
        @Override public String expectedLicence() { return YOUTUBE_LICENCE; }
        @Override public String expectedCategory() { return "Entertainment"; }
        // @formatter:on
    }

    public static class DescriptionTestUnboxing extends DefaultStreamExtractorTest {
        private static final String ID = "cV5TjZCJkuA";
        private static final String URL = BASE_URL + ID;
        private static StreamExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "unboxing"));
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
        @Override public long expectedUploaderSubscriberCountAtLeast() { return 18_000_000; }
        @Override public List<String> expectedDescriptionContains() {
            return Arrays.asList("https://www.youtube.com/watch?v=X7FLCHVXpsA&amp;list=PL7u4lWXQ3wfI_7PgX0C-VTiwLeu0S4v34",
                    "https://www.youtube.com/watch?v=Lqv6G0pDNnw&amp;list=PL7u4lWXQ3wfI_7PgX0C-VTiwLeu0S4v34",
                    "https://www.youtube.com/watch?v=XxaRBPyrnBU&amp;list=PL7u4lWXQ3wfI_7PgX0C-VTiwLeu0S4v34",
                    "https://www.youtube.com/watch?v=U-9tUEOFKNU&amp;list=PL7u4lWXQ3wfI_7PgX0C-VTiwLeu0S4v34");
        }
        @Override public long expectedLength() { return 434; }
        @Override public long expectedViewCountAtLeast() { return 21229200; }
        @Nullable @Override public String expectedUploadDate() { return "2018-06-19 00:00:00.000"; }
        @Nullable @Override public String expectedTextualUploadDate() { return "2018-06-19"; }
        @Override public long expectedLikeCountAtLeast() { return 340100; }
        @Override public long expectedDislikeCountAtLeast() { return -1; }
        @Override public boolean expectedUploaderVerified() { return true; }
        @Override public String expectedLicence() { return YOUTUBE_LICENCE; }
        @Override public String expectedCategory() { return "Science & Technology"; }
        @Override public List<String> expectedTags() {
            return Arrays.asList("2018", "8 plus", "apple", "apple iphone", "apple iphone x", "best", "best android",
                    "best smartphone", "cool gadgets", "find", "find x", "find x review", "find x unboxing", "findx",
                    "galaxy s9", "galaxy s9+", "hands on", "iphone 8", "iphone 8 plus", "iphone x", "new iphone", "nex",
                    "oneplus 6", "oppo", "oppo find x", "oppo find x hands on", "oppo find x review",
                    "oppo find x unboxing", "oppo findx", "pixel 2 xl", "review", "samsung", "samsung galaxy",
                    "samsung galaxy s9", "smartphone", "unbox therapy", "unboxing", "vivo", "vivo apex", "vivo nex");
        }
        // @formatter:on
    }

    @Disabled("Test broken, video was made private")
    public static class RatingsDisabledTest extends DefaultStreamExtractorTest {
        private static final String ID = "HRKu0cvrr_o";
        private static final int TIMESTAMP = 17;
        private static final String URL = BASE_URL + ID + "&t=" + TIMESTAMP;
        private static StreamExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "ratingsDisabled"));
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

    public static class StreamSegmentsTestTagesschau extends DefaultStreamExtractorTest {
        // StreamSegment example with single macro-makers panel
        private static final String ID = "KI7fMGRg0Wk";
        private static final String URL = BASE_URL + ID;
        private static StreamExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "streamSegmentsTagesschau"));
            extractor = YouTube.getStreamExtractor(URL);
            extractor.fetchPage();
        }

        // @formatter:off
        @Override public StreamExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return "tagesschau 20:00 Uhr, 17.03.2021"; }
        @Override public String expectedId() { return ID; }
        @Override public String expectedUrlContains() { return BASE_URL + ID; }
        @Override public String expectedOriginalUrlContains() { return URL; }

        @Override public StreamType expectedStreamType() { return StreamType.VIDEO_STREAM; }
        @Override public String expectedUploaderName() { return "tagesschau"; }
        @Override public String expectedUploaderUrl() { return "https://www.youtube.com/channel/UC5NOEUbkLheQcaaRldYW5GA"; }
        @Override public long expectedUploaderSubscriberCountAtLeast() { return 1_000_000; }
        @Override public boolean expectedUploaderVerified() { return true; }
        @Override public List<String> expectedDescriptionContains() {
            return Arrays.asList("Themen der Sendung", "07:15", "Wetter", "Sendung nachträglich bearbeitet");
        }
        @Override public long expectedLength() { return 953; }
        @Override public long expectedViewCountAtLeast() { return 270000; }
        @Nullable @Override public String expectedUploadDate() { return "2021-03-17 00:00:00.000"; }
        @Nullable @Override public String expectedTextualUploadDate() { return "2021-03-17"; }
        @Override public long expectedLikeCountAtLeast() { return 2300; }
        @Override public long expectedDislikeCountAtLeast() { return -1; }
        @Override public boolean expectedHasSubtitles() { return false; }
        @Override public int expectedStreamSegmentsCount() { return 13; }
        @Override public String expectedLicence() { return YOUTUBE_LICENCE; }
        @Override public String expectedCategory() { return "News & Politics"; }
        // @formatter:on

        @Test
        void testStreamSegment0() throws Exception {
            final StreamSegment segment = extractor.getStreamSegments().get(0);
            assertEquals(0, segment.getStartTimeSeconds());
            assertEquals("Guten Abend", segment.getTitle());
            assertEquals(BASE_URL + ID + "?t=0", segment.getUrl());
            assertNotNull(segment.getPreviewUrl());
        }

        @Test
        void testStreamSegment3() throws Exception {
            final StreamSegment segment = extractor.getStreamSegments().get(3);
            assertEquals(224, segment.getStartTimeSeconds());
            assertEquals("Pandemie dämpft Konjunkturprognose für 2021", segment.getTitle());
            assertEquals(BASE_URL + ID + "?t=224", segment.getUrl());
            assertNotNull(segment.getPreviewUrl());
        }
    }

    public static class StreamSegmentsTestMaiLab extends DefaultStreamExtractorTest {
        // StreamSegment example with macro-makers panel and transcription panel
        private static final String ID = "ud9d5cMDP_0";
        private static final String URL = BASE_URL + ID;
        private static StreamExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "streamSegmentsMaiLab"));
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
        @Override public long expectedUploaderSubscriberCountAtLeast() { return 1_400_000; }
        @Override public List<String> expectedDescriptionContains()  {return Arrays.asList("Vitamin", "2:44", "Was ist Vitamin D?");}
        @Override public boolean expectedUploaderVerified() { return true; }
        @Override public long expectedLength() { return 1010; }
        @Override public long expectedViewCountAtLeast() { return 815500; }
        @Nullable @Override public String expectedUploadDate() { return "2020-11-18 00:00:00.000"; }
        @Nullable @Override public String expectedTextualUploadDate() { return "2020-11-18"; }
        @Override public long expectedLikeCountAtLeast() { return 48500; }
        @Override public long expectedDislikeCountAtLeast() { return -1; }
        @Override public boolean expectedHasSubtitles() { return true; }
        @Override public int expectedStreamSegmentsCount() { return 7; }
        @Override public String expectedLicence() { return YOUTUBE_LICENCE; }
        @Override public String expectedCategory() { return "Science & Technology"; }
        @Override public List<String> expectedTags() {
            return Arrays.asList("Diabetes", "Erkältung", "Gesundheit", "Immunabwehr", "Immunsystem", "Infektion",
                    "Komisch alles chemisch", "Krebs", "Lab", "Lesch", "Mai", "Mai Thi", "Mai Thi Nguyen-Kim",
                    "Mangel", "Nahrungsergänzungsmittel", "Nguyen", "Nguyen Kim", "Nguyen-Kim", "Quarks", "Sommer",
                    "Supplemente", "Supplements", "Tabletten", "Terra X", "TerraX", "The Secret Life Of Scientists",
                    "Tropfen", "Vitamin D", "Vitamin-D-Mangel", "Vitamine", "Winter", "einnehmen", "maiLab", "nehmen",
                    "supplementieren", "Überdosis", "Überschuss");
        }
        // @formatter:on

        @Test
        void testStreamSegment() throws Exception {
            final StreamSegment segment = extractor.getStreamSegments().get(1);
            assertEquals(164, segment.getStartTimeSeconds());
            assertEquals("Was ist Vitamin D?", segment.getTitle());
            assertEquals(BASE_URL + ID + "?t=164", segment.getUrl());
            assertNotNull(segment.getPreviewUrl());
        }

        @Override
        @Test
        @Disabled("encoding problem")
        public void testName() {}

        @Override
        @Test
        @Disabled("encoding problem")
        public void testTags() {}
    }

    public static class PublicBroadcasterTest extends DefaultStreamExtractorTest {
        private static final String ID = "q6fgbYWsMgw";
        private static final int TIMESTAMP = 0;
        private static final String URL = BASE_URL + ID;
        private static StreamExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "publicBroadcast"));
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
        @Override public long expectedUploaderSubscriberCountAtLeast() { return 1_500_000; }
        @Override public List<String> expectedDescriptionContains() { return Arrays.asList("Lasst uns abtauchen!", "Angebot von funk", "Dinge"); }
        @Override public long expectedLength() { return 631; }
        @Override public long expectedTimestamp() { return TIMESTAMP; }
        @Override public long expectedViewCountAtLeast() { return 1_600_000; }
        @Nullable @Override public String expectedUploadDate() { return "2019-06-12 00:00:00.000"; }
        @Nullable @Override public String expectedTextualUploadDate() { return "2019-06-12"; }
        @Override public long expectedLikeCountAtLeast() { return 70000; }
        @Override public long expectedDislikeCountAtLeast() { return -1; }
        @Override public List<MetaInfo> expectedMetaInfo() throws MalformedURLException {
            return Collections.singletonList(new MetaInfo(
                    "",
                    new Description("Funk is a German public broadcast service.", Description.PLAIN_TEXT),
                    Collections.singletonList(new URL("https://de.wikipedia.org/wiki/Funk_(Medienangebot)?wprov=yicw1")),
                    Collections.singletonList("Wikipedia (German)")
            ));
        }
        @Override public boolean expectedUploaderVerified() { return true; }
        @Override public String expectedLicence() { return YOUTUBE_LICENCE; }
        @Override public String expectedCategory() { return "Education"; }
        @Override public List<String> expectedTags() {
            return Arrays.asList("Abgrund", "Algen", "Bakterien", "Challengertief", "Dumbooktopus",
                    "Dunkel", "Dunkelheit", "Fische", "Flohkrebs", "Hadal-Zone", "Kontinentalschelf",
                    "Licht", "Mariannengraben", "Meer", "Meeresbewohner", "Meeresschnee", "Mesopelagial",
                    "Ozean", "Photosynthese", "Plankton", "Plastik", "Polypen", "Pottwale",
                    "Staatsquelle", "Tauchen", "Tauchgang", "Tentakel", "Tiefe", "Tiefsee", "Tintenfische",
                    "Titanic", "Vampirtintenfisch", "Verschmutzung", "Viperfisch", "Wale");
        }
        // @formatter:on
    }

    public static class NoVisualMetadataVideoTest extends DefaultStreamExtractorTest {
        // Video without visual metadata on YouTube clients (video title, upload date, channel name,
        // comments, ...)
        private static final String ID = "An8vtD1FDqs";
        private static final String URL = BASE_URL + ID;
        private static StreamExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "noVisualMetadata"));
            extractor = YouTube.getStreamExtractor(URL);
            extractor.fetchPage();
        }

        @Override public StreamType expectedStreamType() { return StreamType.VIDEO_STREAM; }
        @Override public String expectedUploaderName() { return "Makani"; }
        @Override public String expectedUploaderUrl() { return "https://www.youtube.com/channel/UC-iMZJ8NppwT2fLwzFWJKOQ"; }
        @Override public List<String> expectedDescriptionContains() { return Arrays.asList("Makani", "prototype", "rotors"); }
        @Override public long expectedLength() { return 175; }
        @Override public long expectedViewCountAtLeast() { return 88_000; }
        @Nullable @Override public String expectedUploadDate() { return "2017-05-16 00:00:00.000"; }
        @Nullable @Override public String expectedTextualUploadDate() { return "2017-05-16"; }
        @Override public long expectedLikeCountAtLeast() { return -1; }
        @Override public long expectedDislikeCountAtLeast() { return -1; }
        @Override public StreamExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return YouTube; }
        @Override public String expectedName() { return "Makani’s first commercial-scale energy kite"; }
        @Override public String expectedId() { return "An8vtD1FDqs"; }
        @Override public String expectedUrlContains() { return BASE_URL + ID; }
        @Override public String expectedOriginalUrlContains() { return URL; }
        @Override public String expectedCategory() { return "Science & Technology"; }
        @Override public String expectedLicence() { return YOUTUBE_LICENCE; }
        @Override public List<String> expectedTags() {
            return Arrays.asList("Makani", "Moonshot", "Moonshot Factory", "Prototyping",
                    "california", "california wind", "clean", "clean energy", "climate change",
                    "climate crisis", "energy", "energy kite", "google", "google x", "green",
                    "green energy", "kite", "kite power", "kite power solutions",
                    "kite power systems", "makani power", "power", "renewable", "renewable energy",
                    "renewable energy engineering", "renewable energy projects",
                    "renewable energy sources", "renewables", "solutions", "tech", "technology",
                    "turbine", "wind", "wind energy", "wind power", "wind turbine", "windmill");
        }

        @Test
        @Override
        public void testSubscriberCount() {
            assertThrows(ParsingException.class, () -> extractor.getUploaderSubscriberCount());
        }

        @Test
        @Override
        public void testLikeCount() {
            assertThrows(ParsingException.class, () -> extractor.getLikeCount());
        }

        @Test
        @Override
        public void testUploaderAvatarUrl() {
            assertThrows(ParsingException.class, () -> extractor.getUploaderAvatarUrl());
        }
    }

    public static class UnlistedTest {
        private static YoutubeStreamExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = (YoutubeStreamExtractor) YouTube
                    .getStreamExtractor("https://www.youtube.com/watch?v=tjz2u2DiveM");
            extractor.fetchPage();
        }

        @Test
        void testGetUnlisted() {
            assertEquals(StreamExtractor.Privacy.UNLISTED, extractor.getPrivacy());
        }
    }

    public static class CCLicensed {
        private static final String ID = "M4gD1WSo5mA";
        private static final String URL = BASE_URL + ID;
        private static StreamExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = YouTube.getStreamExtractor(URL);
            extractor.fetchPage();
        }

        @Test
        void testGetLicence() throws ParsingException {
            assertEquals("Creative Commons Attribution licence (reuse allowed)", extractor.getLicence());
        }
    }

    public static class AudioTrackLanguage {

        private static final String ID = "kX3nB4PpJko";
        private static final String URL = BASE_URL + ID;
        private static StreamExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "audioTrack"));
            extractor = YouTube.getStreamExtractor(URL);
            extractor.fetchPage();
        }

        @Test
        void testCheckAudioStreams() throws Exception {
            final List<AudioStream> audioStreams = extractor.getAudioStreams();
            assertFalse(audioStreams.isEmpty());

            for (final AudioStream stream : audioStreams) {
                assertNotNull(stream.getAudioTrackName());
            }

            assertTrue(audioStreams.stream()
                    .anyMatch(audioStream -> "English".equals(audioStream.getAudioTrackName())));

            final Locale hindiLocale = LocaleCompat.forLanguageTag("hi");
            assertTrue(audioStreams.stream()
                    .anyMatch(audioStream ->
                            Objects.equals(audioStream.getAudioLocale(), hindiLocale)));
        }
    }

    public static class DescriptiveAudio {
        private static final String ID = "TjxC-evzxdk";
        private static final String URL = BASE_URL + ID;
        private static StreamExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "descriptiveAudio"));
            extractor = YouTube.getStreamExtractor(URL);
            extractor.fetchPage();
        }

        @Test
        void testCheckDescriptiveAudio() throws Exception {
            assertFalse(extractor.getAudioStreams().isEmpty());

            assertTrue(extractor.getAudioStreams()
                    .stream()
                    .anyMatch(AudioStream::isDescriptive));
        }
    }
}
