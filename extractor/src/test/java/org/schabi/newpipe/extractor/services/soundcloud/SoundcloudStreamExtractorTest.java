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

    public static class LilUziVertDoWhatIWant extends DefaultStreamExtractorTest {
        private static final String ID = "do-what-i-want-produced-by-maaly-raw-don-cannon";
        private static final String UPLOADER = "https://soundcloud.com/liluzivert";
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
        @Override public String expectedName() { return "Do What I Want [Produced By Maaly Raw + Don Cannon]"; }
        @Override public String expectedId() { return "276206960"; }
        @Override public String expectedUrlContains() { return UPLOADER + "/" + ID; }
        @Override public String expectedOriginalUrlContains() { return URL; }

        @Override public StreamType expectedStreamType() { return StreamType.AUDIO_STREAM; }
        @Override public String expectedUploaderName() { return "Lil Uzi Vert"; }
        @Override public String expectedUploaderUrl() { return UPLOADER; }
        @Override public List<String> expectedDescriptionContains() { return Arrays.asList("The Perfect LUV Tape®"); }
        @Override public long expectedLength() { return 175; }
        @Override public long expectedTimestamp() { return TIMESTAMP; }
        @Override public long expectedViewCountAtLeast() { return 75413600; }
        @Nullable @Override public String expectedUploadDate() { return "2016-07-31 18:18:07.000"; }
        @Nullable @Override public String expectedTextualUploadDate() { return "2016-07-31 18:18:07"; }
        @Override public long expectedLikeCountAtLeast() { return -1; }
        @Override public long expectedDislikeCountAtLeast() { return -1; }
        @Override public boolean expectedHasVideoStreams() { return false; }
        @Override public boolean expectedHasSubtitles() { return false; }
        @Override public boolean expectedHasFrames() { return false; }
    }

    public static class ContentNotSupported {
        @BeforeClass
        public static void setUp() {
            NewPipe.init(DownloaderTestImpl.getInstance());
        }

        @Test(expected = ContentNotSupportedException.class)
        public void hlsAudioStream() throws Exception {
            final StreamExtractor extractor =
                    SoundCloud.getStreamExtractor("https://soundcloud.com/dualipa/cool");
            extractor.fetchPage();
            extractor.getAudioStreams();
        }

        @Test(expected = ContentNotSupportedException.class)
        public void bothHlsAndOpusAudioStreams() throws Exception {
            final StreamExtractor extractor =
                    SoundCloud.getStreamExtractor("https://soundcloud.com/lil-baby-4pf/no-sucker");
            extractor.fetchPage();
            extractor.getAudioStreams();
        }
    }
}
