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
        // the first Niconico video
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
        public boolean expectedHasRelatedItems() {
            return false;
        }

        @Override
        public boolean expectedHasAudioStreams() {
            return false;
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

    public static class SM70 extends DefaultStreamExtractorTest {
        // Shorted link (nico.ms)
        private static StreamExtractor extractor;
        private final static String SHORTED_URL = "https://nico.ms/sm70";
        private final static String ACTUAL_URL = "https://www.nicovideo.jp/watch/sm70";

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = Niconico.getStreamExtractor(SHORTED_URL);
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
            return "エージェント夜を往くverとかちつくちて　アイドルマスター";
        }

        @Override
        public String expectedId() throws Exception {
            return "sm70";
        }

        @Override
        public String expectedUrlContains() throws Exception {
            return ACTUAL_URL;
        }

        @Override
        public String expectedOriginalUrlContains() throws Exception {
            return SHORTED_URL;
        }

        @Override
        public StreamType expectedStreamType() {
            return StreamType.VIDEO_STREAM;
        }

        @Override
        public String expectedUploaderName() {
            return "一般会員";
        }

        @Override
        public String expectedUploaderUrl() {
            return "https://www.nicovideo.jp/user/19242";
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
            descs.add("とかちつくちて");
            return descs;
        }

        @Override
        public boolean expectedHasRelatedItems() {
            return false;
        }

        @Override
        public boolean expectedHasAudioStreams() {
            return false;
        }

        @Override
        public long expectedLength() {
            return 126;
        }

        @Override
        public long expectedViewCountAtLeast() {
            return 162631;
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
            return 3446;
        }

        @Override
        public long expectedDislikeCountAtLeast() {
            // Niconico does not have Dislike button
            return -1;
        }
    }

    public static class SO15013657 extends DefaultStreamExtractorTest {
        // Official channel video (starts with "so")
        private static StreamExtractor extractor;
        private final static String URL = "https://www.nicovideo.jp/watch/so15013657";

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
            return "うたの☆プリンスさまっ♪ マジLOVE1000％　メインテーマ／マジLOVE1000％（Op.1バージョン）";
        }

        @Override
        public String expectedId() throws Exception {
            return "so15013657";
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
            return "うたの☆プリンスさまっ♪マジLOVE1000％";
        }

        @Override
        public String expectedUploaderUrl() {
            return "https://ch.nicovideo.jp/ch60034";
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
            descs.add("メインテーマ");
            return descs;
        }

        @Override
        public boolean expectedHasRelatedItems() {
            return false;
        }

        @Override
        public boolean expectedHasAudioStreams() {
            return false;
        }

        @Override
        public long expectedLength() {
            return 138;
        }

        @Override
        public long expectedViewCountAtLeast() {
            return 7784307;
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
            return 2155;
        }

        @Override
        public long expectedDislikeCountAtLeast() {
            // Niconico does not have Dislike button
            return -1;
        }
    }

    public static class SM1715919 extends DefaultStreamExtractorTest {
        // Smartphone page (sp.nicovideo.jp)
        private static StreamExtractor extractor;
        private final static String SHORTED_URL = "https://sp.nicovideo.jp/watch/sm1715919";
        private final static String ACTUAL_URL = "https://www.nicovideo.jp/watch/sm1715919";

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = Niconico.getStreamExtractor(SHORTED_URL);
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
            return "初音ミク　が　オリジナル曲を歌ってくれたよ「メルト」";
        }

        @Override
        public String expectedId() throws Exception {
            return "sm1715919";
        }

        @Override
        public String expectedUrlContains() throws Exception {
            return ACTUAL_URL;
        }

        @Override
        public String expectedOriginalUrlContains() throws Exception {
            return SHORTED_URL;
        }

        @Override
        public StreamType expectedStreamType() {
            return StreamType.VIDEO_STREAM;
        }

        @Override
        public String expectedUploaderName() {
            return "ryo";
        }

        @Override
        public String expectedUploaderUrl() {
            return "https://www.nicovideo.jp/user/317063";
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
            descs.add("恋と戦争においてはあらゆる戦術が許される");
            return descs;
        }

        @Override
        public boolean expectedHasRelatedItems() {
            return false;
        }

        @Override
        public boolean expectedHasAudioStreams() {
            return false;
        }

        @Override
        public long expectedLength() {
            return 256;
        }

        @Override
        public long expectedViewCountAtLeast() {
            return 14791020;
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
            return 6745;
        }

        @Override
        public long expectedDislikeCountAtLeast() {
            // Niconico does not have Dislike button
            return -1;
        }
    }

    public static class SM37761910 extends DefaultStreamExtractorTest {
        // New specification video (max. size 1.5GB)
        private static StreamExtractor extractor;
        private final static String URL = "https://www.nicovideo.jp/watch/sm37761910";

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
            return "【Ado】うっせぇわ";
        }

        @Override
        public String expectedId() throws Exception {
            return "sm37761910";
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
            return "Ado";
        }

        @Override
        public String expectedUploaderUrl() {
            return "https://www.nicovideo.jp/user/39170211";
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
            descs.add("お前が一番うるさいよ");
            return descs;
        }

        @Override
        public boolean expectedHasRelatedItems() {
            return false;
        }

        @Override
        public boolean expectedHasAudioStreams() {
            return false;
        }

        @Override
        public long expectedLength() {
            return 202;
        }

        @Override
        public long expectedViewCountAtLeast() {
            return 5592634;
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
            return 8996;
        }

        @Override
        public long expectedDislikeCountAtLeast() {
            // Niconico does not have Dislike button
            return -1;
        }
    }
}
