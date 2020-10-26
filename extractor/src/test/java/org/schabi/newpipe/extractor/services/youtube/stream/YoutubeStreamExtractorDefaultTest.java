package org.schabi.newpipe.extractor.services.youtube.stream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.schabi.newpipe.DownloaderTestImpl;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.services.DefaultStreamExtractorTest;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.stream.StreamType;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import static org.junit.Assert.fail;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;

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
    static final String BASE_URL = "https://www.youtube.com/watch?v=";

    public static class NotAvailable {
        @BeforeClass
        public static void setUp() {
            NewPipe.init(DownloaderTestImpl.getInstance());
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
    }

    public static class DescriptionTestPewdiepie extends DefaultStreamExtractorTest {
        private static final String ID = "7PIMiDcwNvc";
        private static final int TIMESTAMP = 17;
        private static final String URL = BASE_URL + ID + "&t=" + TIMESTAMP;
        private static StreamExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = YouTube.getStreamExtractor(URL);
            extractor.fetchPage();
        }

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
        @Override public long expectedLength() { return 381; }
        @Override public long expectedTimestamp() { return TIMESTAMP; }
        @Override public long expectedViewCountAtLeast() { return 26682500; }
        @Nullable @Override public String expectedUploadDate() { return "2019-08-24 00:00:00.000"; }
        @Nullable @Override public String expectedTextualUploadDate() { return "2019-08-24"; }
        @Override public long expectedLikeCountAtLeast() { return 5212900; }
        @Override public long expectedDislikeCountAtLeast() { return 30600; }
    }

    public static class DescriptionTestUnboxing extends DefaultStreamExtractorTest {
        private static final String ID = "cV5TjZCJkuA";
        private static final String URL = BASE_URL + ID;
        private static StreamExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = YouTube.getStreamExtractor(URL);
            extractor.fetchPage();
        }

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
        @Override public long expectedDislikeCountAtLeast() { return 18700; }
    }

    public static class RatingsDisabledTest extends DefaultStreamExtractorTest {
        private static final String ID = "HRKu0cvrr_o";
        private static final int TIMESTAMP = 17;
        private static final String URL = BASE_URL + ID + "&t=" + TIMESTAMP;
        private static StreamExtractor extractor;

        @BeforeClass
        public static void setUp() throws Exception {
            NewPipe.init(DownloaderTestImpl.getInstance());
            extractor = YouTube.getStreamExtractor(URL);
            extractor.fetchPage();
        }

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
    }
}
