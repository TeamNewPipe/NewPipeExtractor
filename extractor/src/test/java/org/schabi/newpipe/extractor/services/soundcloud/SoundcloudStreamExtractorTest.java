package org.schabi.newpipe.extractor.services.soundcloud;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ContentNotSupportedException;
import org.schabi.newpipe.extractor.services.DefaultStreamExtractorTest;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import static org.schabi.newpipe.extractor.ServiceList.SoundCloud;

public class SoundcloudStreamExtractorTest {

    public static class CreativeCommonsPlaysWellWithOthers extends DefaultStreamExtractorTest {
        private static final String ID = "plays-well-with-others-ep-2-what-do-an-army-of-ants-and-an-online-encyclopedia-have-in-common";
        private static final String UPLOADER = "https://soundcloud.com/wearecc";
        private static final int TIMESTAMP = 69;
        private static final String URL = UPLOADER + "/" + ID + "#t=" + TIMESTAMP;
        private static StreamExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = SoundCloud.getStreamExtractor(URL);
            extractor.fetchPage();
        }

        @Override public StreamExtractor extractor() { return extractor; }
        @Override public StreamingService expectedService() { return SoundCloud; }
        @Override public String expectedName() { return "Plays Well with Others, Ep 2: What Do an Army of Ants and an Online Encyclopedia Have in Common?"; }
        @Override public String expectedId() { return "597253485"; }
        @Override public String expectedUrlContains() { return UPLOADER + "/" + ID; }
        @Override public String expectedOriginalUrlContains() { return URL; }

        @Override public StreamType expectedStreamType() { return StreamType.AUDIO_STREAM; }
        @Override public String expectedUploaderName() { return "Creative Commons"; }
        @Override public String expectedUploaderUrl() { return UPLOADER; }
        @Override public List<String> expectedDescriptionContains() { return Arrays.asList("Stigmergy is a mechanism of indirect coordination",
                "All original content in Plays Well with Others is available under a Creative Commons BY license."); }
        @Override public long expectedLength() { return 1400; }
        @Override public long expectedTimestamp() { return TIMESTAMP; }
        @Override public long expectedViewCountAtLeast() { return 27000; }
        @Nullable @Override public String expectedUploadDate() { return "2019-03-28 13:36:18.000"; }
        @Nullable @Override public String expectedTextualUploadDate() { return "2019-03-28 13:36:18"; }
        @Override public long expectedLikeCountAtLeast() { return -1; }
        @Override public long expectedDislikeCountAtLeast() { return -1; }
        @Override public boolean expectedHasVideoStreams() { return false; }
        @Override public boolean expectedHasSubtitles() { return false; }
        @Override public boolean expectedHasFrames() { return false; }
    }

}
