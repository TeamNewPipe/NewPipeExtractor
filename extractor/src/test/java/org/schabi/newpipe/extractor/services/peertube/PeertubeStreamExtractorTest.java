package org.schabi.newpipe.extractor.services.peertube;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.DefaultStreamExtractorTest;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import static org.junit.Assert.assertEquals;
import static org.schabi.newpipe.extractor.ServiceList.PeerTube;

public class PeertubeStreamExtractorTest {
    private static final String BASE_URL = "/videos/watch/";

    public static class WhatIsPeertube extends DefaultStreamExtractorTest {
        private static final String ID = "9c9de5e8-0a1e-484a-b099-e80766180a6d";
        private static final String INSTANCE = "https://framatube.org";
        private static final int TIMESTAMP_MINUTE = 1;
        private static final int TIMESTAMP_SECOND = 21;
        private static final String URL = INSTANCE + BASE_URL + ID + "?start=" + TIMESTAMP_MINUTE + "m" + TIMESTAMP_SECOND + "s";
        private static StreamExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            // setting instance might break test when running in parallel (!)
            PeerTube.setInstance(new PeertubeInstance(INSTANCE, "FramaTube"));
            extractor = PeerTube.getStreamExtractor(URL);
            extractor.fetchPage();
        }

        @Test
        public void testGetLanguageInformation() throws ParsingException {
            assertEquals(new Locale("en"), extractor.getLanguageInfo());
        }

        @Override public StreamExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return PeerTube; }
        @Override public String expectedName() { return "What is PeerTube?"; }
        @Override public String expectedId() { return ID; }
        @Override public String expectedUrlContains() { return INSTANCE + BASE_URL + ID; }
        @Override public String expectedOriginalUrlContains() { return URL; }

        @Override public StreamType expectedStreamType() { return StreamType.VIDEO_STREAM; }
        @Override public String expectedUploaderName() { return "Framasoft"; }
        @Override public String expectedUploaderUrl() { return "https://framatube.org/accounts/framasoft@framatube.org"; }
        @Override public List<String> expectedDescriptionContains() { // CRLF line ending
            return Arrays.asList("**[Want to help to translate this video?](https://weblate.framasoft.org/projects/what-is-peertube-video/)**\r\n"
                    + "\r\n"
                    + "**Take back the control of your videos! [#JoinPeertube](https://joinpeertube.org)**\r\n"
                    + "*A decentralized video hosting network, based on free/libre software!*\r\n"
                    + "\r\n"
                    + "**Animation Produced by:** [LILA](https://libreart.info) - [ZeMarmot Team](https://film.zemarmot.net)\r\n"
                    + "*Directed by* Aryeom\r\n"
                    + "*Assistant* Jehan\r\n"
                    + "**Licence**: [CC-By-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/)\r\n"
                    + "\r\n"
                    + "**Sponsored by** [Framasoft](https://framasoft.org)\r\n"
                    + "\r\n"
                    + "**Music**: [Red Step Forward](http://play.dogmazic.net/song.php?song_id=52491) - CC-By Ken Bushima\r\n"
                    + "\r\n"
                    + "**Movie Clip**: [Caminades 3: Llamigos](http://www.caminandes.com/) CC-By Blender Institute\r\n"
                    + "\r\n"
                    + "**Video sources**: https://gitlab.gnome.org/Jehan/what-is-peertube/");
        }
        @Override public long expectedLength() { return 113; }
        @Override public long expectedTimestamp() { return TIMESTAMP_MINUTE*60 + TIMESTAMP_SECOND; }
        @Override public long expectedViewCountAtLeast() { return 38600; }
        @Nullable @Override public String expectedUploadDate() { return "2018-10-01 12:52:46.396"; } // GMT (!)
        @Nullable @Override public String expectedTextualUploadDate() { return "2018-10-01T10:52:46.396Z"; }
        @Override public long expectedLikeCountAtLeast() { return 120; }
        @Override public long expectedDislikeCountAtLeast() { return 0; }
        @Override public boolean expectedHasAudioStreams() { return false; }
        @Override public boolean expectedHasFrames() { return false; }

    }

    public static class AgeRestricted extends DefaultStreamExtractorTest {
        private static final String ID = "0d501633-f2d9-4476-87c6-71f1c02402a4";
        private static final String INSTANCE = "https://peertube.co.uk";
        private static final String URL = INSTANCE + BASE_URL + ID;
        private static StreamExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());;
            // setting instance might break test when running in parallel (!)
            PeerTube.setInstance(new PeertubeInstance(INSTANCE));
            extractor = PeerTube.getStreamExtractor(URL);
            extractor.fetchPage();
        }

        @Override public StreamExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return PeerTube; }
        @Override public String expectedName() { return "A DPR Combatant Describes how Orders are Given through Russian Officers"; }
        @Override public String expectedId() { return ID; }
        @Override public String expectedUrlContains() { return URL; }
        @Override public String expectedOriginalUrlContains() { return URL; }

        @Override public StreamType expectedStreamType() { return StreamType.VIDEO_STREAM; }
        @Override public String expectedUploaderName() { return "Tomas Berezovskiy"; }
        @Override public String expectedUploaderUrl() { return "https://peertube.co.uk/accounts/tomas_berezovskiy@peertube.iriseden.eu"; }
        @Override public List<String> expectedDescriptionContains() { // LF line ending
            return Arrays.asList("https://en.informnapalm.org/dpr-combatant-describes-orders-given-russian-officers/ "
                    + " The InformNapalm team received another video of a separatist prisoner of war telling about his "
                    + "activities in `Dontesk People’s Republic’ (DPR) structures. The video is old, as the interrogation"
                    + " date is September, but it is the situation described is still relevant and interesting today. In "
                    + "this recording the combatant re-tells how he came to be recruited into the DPR forces, and how "
                    + "they are operating under Russian military command. He expresses remorse for his stupidity. Perhaps"
                    + " he is just saying what he thinks his interrogator wants to hear, perhaps he is speaking from a "
                    + "new understanding?\n"
                    + "\n"
                    + "The video contains a lot of cut and paste (stitching) in places where intelligence data or valuable"
                    + " information has been deleted because it cannot be shared publically. We trust you will understand "
                    + "this necessity.");
        }
        @Override public long expectedLength() { return 512; }
        @Override public long expectedViewCountAtLeast() { return 7; }
        @Nullable @Override public String expectedUploadDate() { return "2019-10-22 08:16:48.982"; } // GMT (!)
        @Nullable @Override public String expectedTextualUploadDate() { return "2019-10-22T06:16:48.982Z"; }
        @Override public long expectedLikeCountAtLeast() { return 3; }
        @Override public long expectedDislikeCountAtLeast() { return 0; }
        @Override public int expectedAgeLimit() { return 18; }
        @Override public boolean expectedHasAudioStreams() { return false; }
        @Override public boolean expectedHasSubtitles() { return false; }
        @Override public boolean expectedHasFrames() { return false; }
    }


    @BeforeClass
    public static void setUp() throws Exception {
        NewPipe.init(DownloaderTestImpl.getInstance());
        PeerTube.setInstance(new PeertubeInstance("https://peertube.cpy.re", "PeerTube test server"));
    }

    @Test
    public void testGetEmptyDescription() throws Exception {
        StreamExtractor extractorEmpty = PeerTube.getStreamExtractor("https://framatube.org/api/v1/videos/d5907aad-2252-4207-89ec-a4b687b9337d");
        extractorEmpty.fetchPage();
        assertEquals("", extractorEmpty.getDescription().getContent());
    }

    @Test
    public void testGetSmallDescription() throws Exception {
        StreamExtractor extractorSmall = PeerTube.getStreamExtractor("https://peertube.cpy.re/videos/watch/d2a5ec78-5f85-4090-8ec5-dc1102e022ea");
        extractorSmall.fetchPage();
        assertEquals("https://www.kickstarter.com/projects/1587081065/nothing-to-hide-the-documentary", extractorSmall.getDescription().getContent());
    }

    @Test
    public void testGetSupportInformation() throws ExtractionException, IOException {
        StreamExtractor supportInfoExtractor = PeerTube.getStreamExtractor("https://framatube.org/videos/watch/ee408ec8-07cd-4e35-b884-fb681a4b9d37");
        supportInfoExtractor.fetchPage();
        assertEquals("https://utip.io/chatsceptique", supportInfoExtractor.getSupportInfo());
    }
}
