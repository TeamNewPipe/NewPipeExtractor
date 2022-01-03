package org.schabi.newpipe.extractor.services.niconico;

import static org.schabi.newpipe.extractor.ServiceList.Niconico;

import org.junit.BeforeClass;
import org.schabi.newpipe.downloader.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.services.DefaultStreamExtractorTest;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class NiconicoStreamExtractorTest {

    public static class SM9 extends DefaultStreamExtractorTest {
        private static StreamExtractor extractor;
        private final static String URL = "https://www.nicovideo.jp/watch/sm9";

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = Niconico.getStreamExtractor(URL);
            extractor.fetchPage();
        }

        @Override
        public StreamExtractor extractor() throws Exception {
            return extractor;
        }

        @Override
        public StreamingService expectedService() throws Exception {
            return Niconico;
        }

        @Override
        public String expectedName() throws Exception {
            return "新・豪血寺一族 -煩悩解放 - レッツゴー！陰陽師";
        }

        @Override
        public String expectedId() throws Exception {
            return "sm9";
        }

        @Override
        public String expectedUrlContains() throws Exception {
            return URL;
        }

        @Override
        public String expectedOriginalUrlContains() throws Exception {
            return URL;
        }

        @Override
        public StreamType expectedStreamType() {
            return StreamType.VIDEO_STREAM;
        }

        @Override
        public String expectedUploaderName() {
            return "中の";
        }

        @Override
        public String expectedUploaderUrl() {
            return "https://www.nicovideo.jp/user/4";
        }

        @Override
        public boolean expectedHasFrames() {
            // Niconico does not support video preview with free account.
            return false;
        }

        @Override
        public boolean expectedHasSubtitles() {
            // Niconico does not support subtitles, but there are uploader's comments.
            return false;
        }

        @Override
        public List<String> expectedDescriptionContains() {
            final List<String> descs = new ArrayList<>();
            descs.add("レッツゴー！陰陽師（フルコーラスバージョン）");
            return descs;
        }

        @Override
        public long expectedLength() {
            return 320;
        }

        @Override
        public long expectedViewCountAtLeast() {
            return 20631137;
        }

        @Nullable
        @Override
        public String expectedUploadDate() {
            return null;
        }

        @Nullable
        @Override
        public String expectedTextualUploadDate() {
            return null;
        }

        @Override
        public long expectedLikeCountAtLeast() {
            return 11097;
        }

        @Override
        public long expectedDislikeCountAtLeast() {
            // Niconico does not have Dislike button
            return -1;
        }
    }
}
