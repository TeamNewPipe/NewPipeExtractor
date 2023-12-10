package org.schabi.newpipe.extractor.services.youtube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertContains;
import static org.schabi.newpipe.extractor.ExtractorAsserts.assertGreater;
import static org.schabi.newpipe.extractor.ServiceList.YouTube;
import static org.schabi.newpipe.extractor.comments.CommentsInfoItem.UNKNOWN_REPLY_COUNT;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.downloader.DownloaderFactory;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.comments.CommentsInfo;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.services.DefaultTests;
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeCommentsExtractor;
import org.schabi.newpipe.extractor.utils.Utils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class YoutubeCommentsExtractorTest {
    private static final String RESOURCE_PATH = DownloaderFactory.RESOURCE_PATH + "services/youtube/extractor/comments/";

    /**
     * Test a "normal" YouTube
     */
    public static class Thomas {
        private static final String url = "https://www.youtube.com/watch?v=D00Au7k3i6o";
        private static final String commentContent = "Category: Education";
        private static YoutubeCommentsExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "thomas"));
            extractor = (YoutubeCommentsExtractor) YouTube
                    .getCommentsExtractor(url);
            extractor.fetchPage();
        }

        @Test
        public void testGetComments() throws IOException, ExtractionException {
            assertTrue(getCommentsHelper(extractor));
        }

        private boolean getCommentsHelper(YoutubeCommentsExtractor extractor) throws IOException, ExtractionException {
            InfoItemsPage<CommentsInfoItem> comments = extractor.getInitialPage();
            boolean result = findInComments(comments, commentContent);

            while (comments.hasNextPage() && !result) {
                comments = extractor.getPage(comments.getNextPage());
                result = findInComments(comments, commentContent);
            }

            return result;
        }

        @Test
        public void testGetCommentsFromCommentsInfo() throws IOException, ExtractionException {
            assertTrue(getCommentsFromCommentsInfoHelper(url));
        }

        private boolean getCommentsFromCommentsInfoHelper(String url) throws IOException, ExtractionException {
            final CommentsInfo commentsInfo = CommentsInfo.getInfo(url);

            assertEquals("Comments", commentsInfo.getName());
            boolean result = findInComments(commentsInfo.getRelatedItems(), commentContent);

            Page nextPage = commentsInfo.getNextPage();
            InfoItemsPage<CommentsInfoItem> moreItems = new InfoItemsPage<>(null, nextPage, null);
            while (moreItems.hasNextPage() && !result) {
                moreItems = CommentsInfo.getMoreItems(YouTube, commentsInfo, nextPage);
                result = findInComments(moreItems.getItems(), commentContent);
                nextPage = moreItems.getNextPage();
            }
            return result;
        }

        @Test
        public void testGetCommentsAllData() throws IOException, ExtractionException {
            InfoItemsPage<CommentsInfoItem> comments = extractor.getInitialPage();
            assertTrue(extractor.getCommentsCount() > 5); // at least 5 comments

            DefaultTests.defaultTestListOfItems(YouTube, comments.getItems(), comments.getErrors());
            for (CommentsInfoItem c : comments.getItems()) {
                assertFalse(Utils.isBlank(c.getUploaderUrl()));
                assertFalse(Utils.isBlank(c.getUploaderName()));
                assertFalse(Utils.isBlank(c.getUploaderAvatarUrl()));
                assertFalse(Utils.isBlank(c.getCommentId()));
                assertFalse(Utils.isBlank(c.getCommentText().getContent()));
                assertFalse(Utils.isBlank(c.getName()));
                assertFalse(Utils.isBlank(c.getTextualUploadDate()));
                assertNotNull(c.getUploadDate());
                assertFalse(Utils.isBlank(c.getThumbnailUrl()));
                assertFalse(Utils.isBlank(c.getUrl()));
                assertTrue(c.getLikeCount() >= 0);
            }
        }

        private boolean findInComments(InfoItemsPage<CommentsInfoItem> comments, String comment) {
            return findInComments(comments.getItems(), comment);
        }

        private boolean findInComments(List<CommentsInfoItem> comments, String comment) {
            for (CommentsInfoItem c : comments) {
                if (c.getCommentText().getContent().contains(comment)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Test a video with an empty comment
     */
    public static class EmptyComment {
        private final static String url = "https://www.youtube.com/watch?v=VM_6n762j6M";
        private static YoutubeCommentsExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "empty"));
            extractor = (YoutubeCommentsExtractor) YouTube
                    .getCommentsExtractor(url);
            extractor.fetchPage();
        }

        @Test
        public void testGetCommentsAllData() throws IOException, ExtractionException {
            final InfoItemsPage<CommentsInfoItem> comments = extractor.getInitialPage();

            DefaultTests.defaultTestListOfItems(YouTube, comments.getItems(), comments.getErrors());
            for (CommentsInfoItem c : comments.getItems()) {
                assertFalse(Utils.isBlank(c.getUploaderUrl()));
                assertFalse(Utils.isBlank(c.getUploaderName()));
                assertFalse(Utils.isBlank(c.getUploaderAvatarUrl()));
                assertFalse(Utils.isBlank(c.getCommentId()));
                assertFalse(Utils.isBlank(c.getName()));
                assertFalse(Utils.isBlank(c.getTextualUploadDate()));
                assertNotNull(c.getUploadDate());
                assertFalse(Utils.isBlank(c.getThumbnailUrl()));
                assertFalse(Utils.isBlank(c.getUrl()));
                assertTrue(c.getLikeCount() >= 0);
                if (c.getCommentId().equals("Ugga_h1-EXdHB3gCoAEC")) { // comment without text
                    assertTrue(Utils.isBlank(c.getCommentText().getContent()));
                } else {
                    assertFalse(Utils.isBlank(c.getCommentText().getContent()));
                }
            }
        }

    }

    public static class HeartedByCreator {
        private final static String url = "https://www.youtube.com/watch?v=tR11b7uh17Y";
        private static YoutubeCommentsExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "hearted"));
            extractor = (YoutubeCommentsExtractor) YouTube
                    .getCommentsExtractor(url);
            extractor.fetchPage();
        }

        @Test
        public void testGetCommentsAllData() throws IOException, ExtractionException {
            final InfoItemsPage<CommentsInfoItem> comments = extractor.getInitialPage();

            DefaultTests.defaultTestListOfItems(YouTube, comments.getItems(), comments.getErrors());

            boolean heartedByUploader = false;

            for (CommentsInfoItem c : comments.getItems()) {
                assertFalse(Utils.isBlank(c.getUploaderUrl()));
                assertFalse(Utils.isBlank(c.getUploaderName()));
                assertFalse(Utils.isBlank(c.getUploaderAvatarUrl()));
                assertFalse(Utils.isBlank(c.getCommentId()));
                assertFalse(Utils.isBlank(c.getName()));
                assertFalse(Utils.isBlank(c.getTextualUploadDate()));
                assertNotNull(c.getUploadDate());
                assertFalse(Utils.isBlank(c.getThumbnailUrl()));
                assertFalse(Utils.isBlank(c.getUrl()));
                assertTrue(c.getLikeCount() >= 0);
                assertFalse(Utils.isBlank(c.getCommentText().getContent()));
                if (c.isHeartedByUploader()) {
                    heartedByUploader = true;
                }
            }
            assertTrue(heartedByUploader, "No comments was hearted by uploader");

        }
    }

    public static class Pinned {
        private final static String url = "https://www.youtube.com/watch?v=bjFtFMilb34";
        private static YoutubeCommentsExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "pinned"));
            extractor = (YoutubeCommentsExtractor) YouTube
                    .getCommentsExtractor(url);
            extractor.fetchPage();
        }

        @Test
        public void testGetCommentsAllData() throws IOException, ExtractionException {
            final InfoItemsPage<CommentsInfoItem> comments = extractor.getInitialPage();

            DefaultTests.defaultTestListOfItems(YouTube, comments.getItems(), comments.getErrors());

            for (CommentsInfoItem c : comments.getItems()) {
                assertFalse(Utils.isBlank(c.getUploaderUrl()));
                assertFalse(Utils.isBlank(c.getUploaderName()));
                assertFalse(Utils.isBlank(c.getUploaderAvatarUrl()));
                assertFalse(Utils.isBlank(c.getCommentId()));
                assertFalse(Utils.isBlank(c.getName()));
                assertFalse(Utils.isBlank(c.getTextualUploadDate()));
                assertNotNull(c.getUploadDate());
                assertFalse(Utils.isBlank(c.getThumbnailUrl()));
                assertFalse(Utils.isBlank(c.getUrl()));
                assertTrue(c.getLikeCount() >= 0);
                assertFalse(Utils.isBlank(c.getCommentText().getContent()));
            }

            assertTrue(comments.getItems().get(0).isPinned(), "First comment isn't pinned");
        }
    }

    /**
     * Checks if the likes/votes are handled correctly<br/>
     * A pinned comment with >15K likes is used for the test
     */
    public static class LikesVotes {
        private final static String url = "https://www.youtube.com/watch?v=QqsLTNkzvaY";
        private static YoutubeCommentsExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "likes"));
            extractor = (YoutubeCommentsExtractor) YouTube
                    .getCommentsExtractor(url);
            extractor.fetchPage();
        }

        @Test
        public void testGetCommentsFirst() throws IOException, ExtractionException {
            final InfoItemsPage<CommentsInfoItem> comments = extractor.getInitialPage();

            DefaultTests.defaultTestListOfItems(YouTube, comments.getItems(), comments.getErrors());

            CommentsInfoItem pinnedComment = comments.getItems().get(0);

            assertTrue(pinnedComment.isPinned(), "First comment isn't pinned");
            assertTrue(pinnedComment.getLikeCount() > 0, "The first pinned comment has no likes");
            assertFalse(Utils.isBlank(pinnedComment.getTextualLikeCount()), "The first pinned comment has no vote count");
        }
    }

    /**
     * Checks if the vote count works localized<br/>
     * A pinned comment with >15K likes is used for the test
     */
    public static class LocalizedVoteCount {
        private final static String url = "https://www.youtube.com/watch?v=QqsLTNkzvaY";
        private static YoutubeCommentsExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "localized_vote_count"));
            extractor = (YoutubeCommentsExtractor) YouTube
                    .getCommentsExtractor(url);
            // Force non english local here
            extractor.forceLocalization(Localization.fromLocale(Locale.GERMANY));
            extractor.fetchPage();
        }

        @Test
        public void testGetCommentsFirst() throws IOException, ExtractionException {
            final InfoItemsPage<CommentsInfoItem> comments = extractor.getInitialPage();

            DefaultTests.defaultTestListOfItems(YouTube, comments.getItems(), comments.getErrors());

            CommentsInfoItem pinnedComment = comments.getItems().get(0);

            assertTrue(pinnedComment.isPinned(), "First comment isn't pinned");
            assertFalse(Utils.isBlank(pinnedComment.getTextualLikeCount()), "The first pinned comment has no vote count");
        }
    }

    public static class RepliesTest {
        private final static String url = "https://www.youtube.com/watch?v=xaQJbozY_Is";
        private static YoutubeCommentsExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "replies"));
            extractor = (YoutubeCommentsExtractor) YouTube
                    .getCommentsExtractor(url);
            extractor.fetchPage();
        }

        @Test
        public void testGetCommentsFirstReplies() throws IOException, ExtractionException {
            final InfoItemsPage<CommentsInfoItem> comments = extractor.getInitialPage();

            DefaultTests.defaultTestListOfItems(YouTube, comments.getItems(), comments.getErrors());

            CommentsInfoItem firstComment = comments.getItems().get(0);

            assertTrue(firstComment.isPinned(), "First comment isn't pinned");

            InfoItemsPage<CommentsInfoItem> replies = extractor.getPage(firstComment.getReplies());

            assertEquals("First", replies.getItems().get(0).getCommentText().getContent(),
                    "First reply comment did not match");
        }

        @Test
        public void testGetCommentsReplyCount() throws IOException, ExtractionException {
            final InfoItemsPage<CommentsInfoItem> comments = extractor.getInitialPage();

            DefaultTests.defaultTestListOfItems(YouTube, comments.getItems(), comments.getErrors());

            final CommentsInfoItem firstComment = comments.getItems().get(0);

            assertNotEquals(UNKNOWN_REPLY_COUNT, firstComment.getReplyCount(), "Could not get the reply count of the first comment");
            assertGreater(300, firstComment.getReplyCount());
        }

        @Test
        public void testCommentsCount() throws IOException, ExtractionException {
            assertTrue(extractor.getCommentsCount() > 18800);
        }
    }

    public static class FormattingTest {

        private final static String url = "https://www.youtube.com/watch?v=zYpyS2HaZHM";

        private static YoutubeCommentsExtractor extractor;

        @BeforeAll
        public static void setUp() throws Exception {
            YoutubeTestsUtils.ensureStateless();
            NewPipe.init(DownloaderFactory.getDownloader(RESOURCE_PATH + "formatting"));
            extractor = (YoutubeCommentsExtractor) YouTube
                    .getCommentsExtractor(url);
            extractor.fetchPage();
        }

        @Test
        public void testGetCommentsFormatting() throws IOException, ExtractionException {
            final InfoItemsPage<CommentsInfoItem> comments = extractor.getInitialPage();

            DefaultTests.defaultTestListOfItems(YouTube, comments.getItems(), comments.getErrors());

            final CommentsInfoItem firstComment = comments.getItems().get(0);

            assertContains("<s>", firstComment.getCommentText().getContent());
            assertContains("<b>", firstComment.getCommentText().getContent());
        }
    }
}
